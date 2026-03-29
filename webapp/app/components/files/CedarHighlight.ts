import hljs, { Mode } from "highlight.js";

const IDENT = "[a-zA-Z_$][a-zA-Z_$0-9]*";

export const CedarHighlight = {
  // or with an array
  keywords: {
    keyword: [
      "permit",
      "forbid",
      "appliesTo",
      "when",
      "unless",
      "is",
      "in",
      "if",
      "then",
      "else",
      "has",
      "like",
      "__cedar",
    ],
    literal: ["true", "false"],
    pragma: [
      "principal",
      "resource",
      "action",
      "context",
      "type",
      "namespace",
      "entity",
    ],
    type: [
      "Bool",
      "Boolean",
      "Entity",
      "Extension",
      "Long",
      "Record",
      "Set",
      "String",
    ],
  },
  contains: [
    hljs.QUOTE_STRING_MODE,
    hljs.C_LINE_COMMENT_MODE,
    hljs.NUMBER_MODE,
    hljs.REGEXP_MODE,
    {
      scope: "annotation",
      begin: "@" + IDENT,
      contains: [
        {
          begin: /\(/,
          end: /\)/,
          contains: [hljs.QUOTE_STRING_MODE],
        },
      ],
    },
    {
      scope: "type",
      begin: IDENT + "::",
      contains: [
        "self",
        hljs.QUOTE_STRING_MODE,
        hljs.UNDERSCORE_TITLE_MODE,
      ] as ("self" | Mode)[],
    },
    {
      scope: "operator",
      begin: /(\|\||&&|==|!=|>=|<=|>|<|!|-|\*|\+)/,
    },
  ],
};
