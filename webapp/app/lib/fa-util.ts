import {
  faSquareMinus as regularMinus,
  faSquarePlus as regularPlus,
} from "@fortawesome/free-regular-svg-icons";
import {
  IconDefinition,
  faBarsStaggered,
  faCheckDouble,
  faDatabase,
  faFileLines,
  faLock,
  faSquareMinus as solidMinus,
  faSquarePlus as solidPlus,
} from "@fortawesome/free-solid-svg-icons";
import { FileType } from "./types";

export function getFileIcon(filetype: FileType | undefined): IconDefinition {
  switch (filetype) {
    case "cedar":
      return faLock;
    case "cedarschema":
      return faBarsStaggered;
    case "entities":
      return faDatabase;
    case "invariant":
      return faCheckDouble;
    default:
      return faFileLines;
  }
}

export function getExpandIcon(
  hovered: boolean,
  expanded: boolean,
): IconDefinition {
  if (expanded) {
    return hovered ? solidMinus : regularMinus;
  } else {
    return hovered ? solidPlus : regularPlus;
  }
}
