
const express = require("express");
const path = require("path");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json({ limit: "1mb" }));

// DEADLY AI home
app.get("/", (req, res) => {
  res.sendFile(path.join(__dirname, "index.html"));
});

// Server health check
app.get("/api/health", (req, res) => {
  res.json({
    success: true,
    assistant: "DEADLY AI",
    status: "online",
    version: "2.0"
  });
});

// AI chat endpoint
app.post("/api/chat", async (req, res) => {
  try {
    const { message } = req.body;

    if (!message || typeof message !== "string") {
      return res.status(400).json({
        success: false,
        error: "Message is required"
      });
    }

    res.json({
      success: true,
      assistant: "DEADLY AI",
      message: "Your request has been received.",
      reply: "DEADLY AI backend is ready. AI model connection is the next step."
    });

  } catch (error) {
    res.status(500).json({
      success: false,
      error: "Internal server error"
    });
  }
});

app.listen(PORT, () => {
  console.log(`DEADLY AI running on port ${PORT}`);
});
