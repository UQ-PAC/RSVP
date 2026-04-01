import hljs, { Mode } from "highlight.js";

const baseJSON = hljs.getLanguage("json")?.rawDefinition?.();

export const EntitiesHighlight = {
  keywords: {
    keyword: ["uid", "parents", "attrs", "tags", "__entity", "type", "id"],
  },
  contains: [
    ...(baseJSON?.contains ?? []),
    {
      scope: "type",
      begin: /(?<="type":)\s*[a-zA-Z_$0-9"]*\s*(?=,)/,
    },
  ],
};
