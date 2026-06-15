#!/usr/bin/env python3
#
# Copyright 2026 Gabriel Henrique Lopes Gomes Alves Nunes.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""
Generates a Graphviz DOT digraph from an OpenFGA store file.

Usage:
    python3 fga_visualize.py childrenclinic.fga.yaml
    python3 fga_visualize.py childrenclinic.fga.yaml -o graph.svg
    python3 fga_visualize.py childrenclinic.fga.yaml -o graph.png
    python3 fga_visualize.py childrenclinic.fga.yaml --types clinic,employee
    python3 fga_visualize.py childrenclinic.fga.yaml --relations assigned_doctor,clinic
    python3 fga_visualize.py childrenclinic.fga.yaml --exclude-types group
    python3 fga_visualize.py childrenclinic.fga.yaml --exclude-relations can_list_employees

Requires: PyYAML.
Optional: graphviz (system package) for rendering.
"""

import argparse
import subprocess
import sys
import textwrap
from collections import defaultdict
from pathlib import Path

import yaml

# Colour palette per type.
TYPE_COLOURS = [
    "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3",
    "#fdb462", "#b3de69", "#fccde5", "#d9d9d9", "#bc80bd",
    "#ccebc5", "#ffed6f",
]

def parse_args():
    p = argparse.ArgumentParser(
        description="Visualise OpenFGA relationship tuples as a Graphviz digraph."
    )
    p.add_argument("store_file", help="Path to childrenclinic.fga.yaml")
    p.add_argument("-o", "--output", help="Output file (svg/png/pdf/dot). "
                   "Format inferred from extension. Omit for DOT on stdout.")
    p.add_argument("--types", help="Comma-separated object types to include.")
    p.add_argument("--relations", help="Comma-separated relations to include.")
    p.add_argument("--exclude-types", help="Comma-separated object types to exclude.")
    p.add_argument("--exclude-relations", help="Comma-separated relations to exclude.")
    p.add_argument("--no-wildcards", action="store_true",
                   help="Omit wildcard (user:*) tuples.")
    p.add_argument("--rankdir", default="LR", choices=["LR", "TB", "BT", "RL"],
                   help="Graph direction (default: LR).")
    return p.parse_args()

def load_tuples(path):
    with open(path) as f:
        data = yaml.safe_load(f)
    return data.get("tuples", [])

def split_ref(ref):
    """Split 'type:id' into (type, id). Handles 'user:*'."""
    colon = ref.index(":")
    return ref[:colon], ref[colon + 1:]

def build_dot(tuples, *, rankdir="LR", no_wildcards=False,
              include_types=None, exclude_types=None,
              include_relations=None, exclude_relations=None):

    # Filter tuples.
    filtered = []
    for t in tuples:
        obj_type, _ = split_ref(t["object"])
        rel = t["relation"]
        user_str = t["user"]
        is_wildcard = user_str.endswith(":*")

        if no_wildcards and is_wildcard:
            continue
        if include_types and obj_type not in include_types:
            continue
        if exclude_types and obj_type in exclude_types:
            continue
        if include_relations and rel not in include_relations:
            continue
        if exclude_relations and rel in exclude_relations:
            continue
        filtered.append(t)

    # Collect all nodes grouped by type.
    type_nodes = defaultdict(set)
    for t in filtered:
        obj_type, obj_id = split_ref(t["object"])
        type_nodes[obj_type].add(obj_id)

        user_str = t["user"]
        if user_str.endswith(":*"):
            u_type, _ = split_ref(user_str)
            type_nodes[u_type].add("*")
        else:
            u_type, u_id = split_ref(user_str)
            type_nodes[u_type].add(u_id)

    # Assign colours.
    sorted_types = sorted(type_nodes.keys())
    colour_map = {}
    for i, t in enumerate(sorted_types):
        colour_map[t] = TYPE_COLOURS[i % len(TYPE_COLOURS)]

    # Build DOT.
    lines = []
    lines.append(f'strict digraph {{')
    lines.append(f'  rankdir="{rankdir}"')
    lines.append(f'  graph [fontname="DejaVu Sans" fontsize=11 '
                 f'bgcolor="transparent" pad=0.4 nodesep=0.35 ranksep=0.6]')
    lines.append(f'  node [shape=box style="filled,rounded" '
                 f'fontname="DejaVu Sans" fontsize=10 margin="0.12,0.06"]')
    lines.append(f'  edge [fontname="DejaVu Sans" fontsize=8 '
                 f'color="#555555" fontcolor="#333333"]')

    # Subgraph clusters.
    for typ in sorted_types:
        safe_cluster = typ.replace(":", "_")
        fill = colour_map[typ]
        lines.append(f'  subgraph "cluster_{safe_cluster}" {{')
        lines.append(f'    label=<<B>{typ}</B>>')
        lines.append(f'    style="rounded,filled" fillcolor="{fill}40" '
                     f'color="{fill}" penwidth=1.5')
        lines.append(f'    fontname="DejaVu Sans" fontsize=11')
        for nid in sorted(type_nodes[typ]):
            node_key = f'{typ}:{nid}'
            display = nid if nid != "*" else f"✱ (any {typ})"
            style = 'style="filled,rounded,dashed"' if nid == "*" else 'style="filled,rounded"'
            lines.append(f'    "{node_key}" [label="{display}" '
                         f'fillcolor="{fill}" {style}]')
        lines.append(f'  }}')

    # Edges.
    for t in filtered:
        user_str = t["user"]
        obj_str = t["object"]
        rel = t["relation"]
        is_wildcard = user_str.endswith(":*")
        style = ' style="dashed"' if is_wildcard else ""
        lines.append(f'  "{obj_str}" -> "{user_str}" '
                     f'[label=" {rel}"{style}]')

    lines.append("}")
    return "\n".join(lines)

def render(dot_source, output_path):
    """Render DOT to a file using the system `dot` command."""
    suffix = Path(output_path).suffix.lstrip(".")
    if suffix == "dot":
        Path(output_path).write_text(dot_source, encoding="utf-8")
        return
    try:
        proc = subprocess.run(
            ["dot", f"-T{suffix}", "-o", output_path],
            input=dot_source, text=True, capture_output=True, check=True,
        )
    except FileNotFoundError:
        print("Error: 'dot' not found. Install Graphviz or use `-o file.dot`.",
              file=sys.stderr)
        sys.exit(1)
    except subprocess.CalledProcessError as e:
        print(f"Graphviz error: {e.stderr}", file=sys.stderr)
        sys.exit(1)

def main():
    args = parse_args()

    tuples = load_tuples(args.store_file)

    include_types = set(args.types.split(",")) if args.types else None
    exclude_types = set(args.exclude_types.split(",")) if args.exclude_types else None
    include_rels = set(args.relations.split(",")) if args.relations else None
    exclude_rels = set(args.exclude_relations.split(",")) if args.exclude_relations else None

    dot = build_dot(
        tuples,
        rankdir=args.rankdir,
        no_wildcards=args.no_wildcards,
        include_types=include_types,
        exclude_types=exclude_types,
        include_relations=include_rels,
        exclude_relations=exclude_rels,
    )

    if args.output:
        render(dot, args.output)
    else:
        print(dot)

if __name__ == "__main__":
    main()
