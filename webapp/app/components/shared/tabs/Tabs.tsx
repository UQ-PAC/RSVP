"use client";

import { JSX, useEffect, useRef, useState } from "react";
import { Tab, TabTheme } from "./Tab";

import "./tabs.css";

interface TabsParams {
  options: number;
  defaultOption?: number;
  overrideOption?: number;
  onSelect: (option: number) => void;
  tabContent: (option: number) => JSX.Element | JSX.Element[] | undefined;
  tabTheme?: (option: number) => TabTheme | undefined;
}

export function Tabs({
  options,
  defaultOption,
  overrideOption,
  onSelect,
  tabContent,
  tabTheme = () => undefined,
}: TabsParams) {
  const [selectedOption, setSelectedOption] = useState<number>(
    defaultOption ?? 0,
  );

  const selected = overrideOption ?? selectedOption;

  // Element references for automatic scroll
  const container = useRef<HTMLDivElement>(null);
  const selectedTab = useRef<HTMLDivElement>(null);

  // On selection, if selected tab is hidden then scroll it into view
  useEffect(() => {
    scrollTabIfNeeded(selectedTab.current, container.current);
  }, [selected]);

  return (
    <div
      className="tabs"
      ref={container}
      onWheel={(e) => {
        e.preventDefault();
        e.stopPropagation();
        container.current?.scrollTo({
          left: container.current.scrollLeft + e.deltaY * 3,
          behavior: "smooth",
        });
      }}
      data-testid="tabs"
    >
      {[...Array(options).keys()].map((i) => (
        <Tab
          key={i}
          ref={selected === i ? selectedTab : null}
          selected={selected === i}
          select={() => {
            setSelectedOption(i);
            onSelect(i);
          }}
          theme={tabTheme(i)}
        >
          {tabContent(i)}
        </Tab>
      ))}
    </div>
  );
}

function scrollTabIfNeeded(
  tab: HTMLDivElement | null,
  container: HTMLDivElement | null,
): void {
  if (tab && container) {
    const tabOffset = tab.offsetLeft - container.offsetLeft;

    if (
      tabOffset + tab.offsetWidth * 0.95 < container.scrollLeft ||
      tabOffset > container.scrollLeft + container.offsetWidth * 0.98
    ) {
      tab.scrollIntoView({
        block: "center",
        behavior: "instant",
      });
    }
  }
}
