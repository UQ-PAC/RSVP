import type { Metadata } from "next";
import { Roboto_Flex } from "next/font/google";
import "./globals.css";
import "./components/files/CedarHighlight";

const robotoFlex = Roboto_Flex({
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "RSVP",
  description: "Policy verification",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${robotoFlex.className} antialiased`}>{children}</body>
    </html>
  );
}
