import express from "express";

const PORT = process.env.PORT || 3001;

const app = express();

app.use(express.json());

app.get("/api/hello", (req, res) => {
  res.json({ message: "Hello!" });
});

app.listen(PORT, () => {
  console.log(`Server listening on ${PORT}`);
});
