import hljs, { Mode } from "highlight.js";
import "./syntax.css";

const IDENT = "[a-zA-Z_$][a-zA-Z_$0-9]*";

hljs.registerLanguage("cedar", () => ({
  keywords: {
    keyword: [
      "permit",
      "forbid",
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
    pragma: ["principal", "resource", "action", "context", "type", "namespace"],
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
      scope: "type",
      begin: /(?<=is)\s+[^\s),]+/,
    },
    {
      scope: "operator",
      begin: /(\|\||&&|==|!=|>=|<=|>|<|!|-|\*|\+)/,
    },
  ],
}));

hljs.registerLanguage("cedarschema", () => ({
  keywords: {
    keyword: [
      "appliesTo",
      "__cedar",
      "enum",
      "entity",
      "action",
      "namespace",
      "type",
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
      begin: /(?<=:)\s*[^{}:,<>\n]+/,
    },
    {
      scope: "type",
      begin: /(?<=<)\s*[^:,<>\n]+(?=>)/,
    },
    {
      scope: "type",
      begin: /(?<=entity)\s+[^\s]+(?=\s)/,
    },
    {
      scope: "type",
      begin: /(?<=type)\s+[^\s]+(?=\s)/,
    },
    {
      scope: "type",
      begin: /(?<=action)\s+[^\s]+\s+(?=appliesTo)/,
    },
    {
      scope: "pragma",
      begin: /^\s+(principal|resource|context)\s*(?=:)/m,
    },
    {
      scope: "key",
      begin: /^\s+[^\s]+\s*(?=:)/m,
    },
  ],
}));

hljs.registerLanguage("invariant", () => ({
  keywords: {
    keyword: ["allow", "deny", "for", "none", "some", "all"],
    literal: ["true", "false"],
  },
  contains: [
    hljs.QUOTE_STRING_MODE,
    hljs.C_LINE_COMMENT_MODE,
    hljs.NUMBER_MODE,
    hljs.REGEXP_MODE,
    {
      scope: "annotation",
      begin: "@invariant",
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
      scope: "type",
      begin: "(?<=:)\s*" + IDENT + "(?=[,;])",
    },
    {
      scope: "type",
      begin: /(?<=:)\s*\w+\s*(?=[;,])/,
    },
    {
      scope: "operator",
      begin: /(\|\||-->|&&|==|!=|>=|<=|>|<|!|-|\*|\+)/,
    },
  ],
}));

hljs.registerLanguage("entities", () => ({
  keywords: {
    literal: ["true", "false"],
  },
  contains: [
    {
      scope: "key-keyword",
      begin: /\"(uid|parents|attrs|tags|__entity|type|id)\s?\"(?=:)/,
    },
    {
      scope: "key",
      begin: /\"[^"]+\"\s?(?=:)/,
    },
    {
      scope: "type",
      begin: /(?<="type":)\s*[a-zA-Z_$0-9"]*\s*(?=,)/,
    },
    hljs.QUOTE_STRING_MODE,
    hljs.NUMBER_MODE,
  ],
}));
