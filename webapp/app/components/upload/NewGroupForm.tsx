"use client";

import { faCirclePlus, faCircleXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import cx from "classnames";
import { useState } from "react";

interface NewGroupFormParams {
  index: number;
  create: (name: string) => void;
  cancel: () => void;
  existing: string[];
}

export function NewGroupForm({
  index,
  create,
  cancel,
  existing,
}: NewGroupFormParams) {
  const placeholder = `Policy Set ${index}`;

  const [name, setName] = useState(placeholder);

  const invalid = existing.some(
    (existing) => existing.toLowerCase() === name.toLowerCase(),
  );

  return (
    <form className="create-analysis-group-form">
      <input
        className={cx("create-analysis-group-text-input", invalid && "invalid")}
        type="text"
        placeholder={placeholder}
        onChange={(e) => {
          const value = e.target.value;
          if (value.length === 0) {
            setName(placeholder);
          } else {
            setName(value);
          }
        }}
      />
      {invalid && (
        <span className="input-error-message-container">
          <span className="input-error-message">Policy set already exists</span>
        </span>
      )}
      <span className="create-analysis-group-buttons">
        <button
          className="cancel-create-analysis-group-button"
          onClick={(e) => {
            e.preventDefault();
            cancel();
          }}
        >
          <FontAwesomeIcon className="button-icon" icon={faCircleXmark} />
          <span className="button-text">Cancel</span>
        </button>
        <button
          className="create-analysis-group-button"
          disabled={invalid}
          onClick={(e) => {
            e.preventDefault();
            create(name);
          }}
        >
          <FontAwesomeIcon className="button-icon" icon={faCirclePlus} />
          <span className="button-text">Create</span>
        </button>
      </span>
    </form>
  );
}
