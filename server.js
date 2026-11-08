
const express = require("express");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json({ limit: "1mb" }));
app.use(express.static(__dirname));

const providers = {
  groq: {
    key: "GROQ_API_KEY",
    url: "https://api.groq.com/openai/v1/chat/completions",
    model: "llama-3.3-70b-versatile"
  },
  openai: {
    key: "OPENAI_API_KEY",
    url: "https://api.openai.com/v1/chat/completions",
    model: "gpt-4.1-mini"
  },
  xai: {
    key: "XAI_API_KEY",
    url: "https://api.x.ai/v1/chat/completions",
    model: "grok-3-mini"
  },
  claude: {
    key: "ANTHROPIC_API_KEY",
    model: "claude-sonnet-4-5"
  },
  gemini: {
    key: "GEMINI_API_KEY",
    model: "gemini-2.5-flash"
  }
};

app.get("/", (req, res) => {
  res.sendFile(__dirname + "/index.html");
});

app.get("/api/health", (req, res) => {
  res.json({
    success: true,
    assistant: "DEADLY AI",
    status: "online",
    version: "2.1"
  });
});

app.get("/api/providers", (req, res) => {
  const result = {};

  for (const [name, p] of Object.entries(providers)) {
    result[name] = {
      configured: !!process.env[p.key],
      model: process.env[p.modelEnv] || p.model
    };
  }

  res.json({ success: true, providers: result });
});

app.post("/api/chat", async (req, res) => {
  try {
    const message = req.body.message?.trim();
    const provider = req.body.provider || "groq";
    const p = providers[provider];

    if (!message) {
      return res.status(400).json({
        success: false,
        error: "Message is required."
      });
    }

    if (!p) {
      return res.status(400).json({
        success: false,
        error: "Unknown AI provider."
      });
    }

    const key = process.env[p.key];

    if (!key) {
      return res.status(503).json({
        success: false,
        error: "API key missing: " + p.key
      });
    }

    const model = process.env[p.modelEnv] || p.model;
    const systemPrompt =
      "You are DEADLY AI, a helpful personal assistant. " +
      "Reply in Hindi, English or Hinglish according to the user. " +
      "Be honest and never claim an action succeeded unless verified.";

    let response;
    let reply;

    if (["groq", "openai", "xai"].includes(provider)) {
      response = await fetch(p.url, {
        method: "POST",
        headers: {
          Authorization: "Bearer " + key,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model,
          messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: message }
          ]
        })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.error?.message || "AI provider error"
        );
      }

      reply = data.choices?.[0]?.message?.content;
    }

    if (provider === "claude") {
      response = await fetch(
        "https://api.anthropic.com/v1/messages",
        {
          method: "POST",
          headers: {
            "x-api-key": key,
            "anthropic-version": "2023-06-01",
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            model,
            max_tokens: 1200,
            system: systemPrompt,
            messages: [
              { role: "user", content: message }
            ]
          })
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.error?.message || "Claude API error"
        );
      }

      reply = (data.content || [])
        .filter(x => x.type === "text")
        .map(x => x.text)
        .join("\n");
    }

    if (provider === "gemini") {
      response = await fetch(
        "https://generativelanguage.googleapis.com/v1beta/models/" +
        encodeURIComponent(model) + ":generateContent",
        {
          method: "POST",
          headers: {
            "x-goog-api-key": key,
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            systemInstruction: {
              parts: [{ text: systemPrompt }]
            },
            contents: [{
              role: "user",
              parts: [{ text: message }]
            }]
          })
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.error?.message || "Gemini API error"
        );
      }

      reply = data.candidates?.[0]?.content?.parts
        ?.map(x => x.text || "")
        .join("\n");
    }

    if (!reply) {
      throw new Error("Empty response from AI model.");
    }

    res.json({
      success: true,
      assistant: "DEADLY AI",
      provider,
      model,
      reply
    });

  } catch (error) {
    console.error(error.message);

    res.status(502).json({
      success: false,
      error: error.message
    });
  }
});

app.listen(PORT, () => {
  console.log("DEADLY AI running on port " + PORT);


  {
  "status": "online",
  "version": "2.0"
}
});
