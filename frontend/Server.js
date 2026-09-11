import express from "express";
import cors from "cors";
import dotenv from "dotenv";

import { GoogleGenAI } from "@google/genai";

dotenv.config();

const app = express();

app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;


/*
========================================================
GEMINI CLIENT
========================================================
*/

const ai = new GoogleGenAI({
    apiKey: process.env.GEMINI_API_KEY
});


/*
========================================================
JARVIS SYSTEM INSTRUCTIONS
========================================================
*/

const SYSTEM_INSTRUCTION = `
You are JARVIS, a personal AI assistant.

Personality:
- Intelligent
- Calm
- Helpful
- Concise
- Friendly
- Professional

You can interact with external tools.

IMPORTANT:
You do not directly execute tools.
You request a tool and the application executes it.

Never claim an action succeeded unless the application
returns a successful result.

For potentially important actions, explain what happened.

Available capabilities include:
- Opening supported applications
- Opening websites
- Searching YouTube
- Calendar operations
- Android device commands

Keep responses natural and suitable for voice.
`;


/*
========================================================
TOOLS
========================================================
*/

const tools = [

    {
        type: "function",

        name: "open_app",

        description:
            "Open a supported Android application.",

        parameters: {
            type: "object",

            properties: {

                app: {
                    type: "string",
                    description:
                        "Application name such as YouTube, Instagram, Snapchat, Google or Camera."
                }

            },

            required: ["app"]
        }
    },


    {
        type: "function",

        name: "open_website",

        description:
            "Open a website in the user's browser.",

        parameters: {

            type: "object",

            properties: {

                url: {
                    type: "string",
                    description:
                        "HTTPS website URL."
                }

            },

            required: ["url"]
        }
    },


    {
        type: "function",

        name: "search_youtube",

        description:
            "Search YouTube for a query.",

        parameters: {

            type: "object",

            properties: {

                query: {
                    type: "string"
                }

            },

            required: ["query"]
        }
    },


    {
        type: "function",

        name: "get_calendar_events",

        description:
            "Get calendar events for a date.",

        parameters: {

            type: "object",

            properties: {

                date: {
                    type: "string",
                    description:
                        "Date in YYYY-MM-DD format."
                }

            },

            required: ["date"]
        }
    },


    {
        type: "function",

        name: "create_calendar_event",

        description:
            "Create a calendar event.",

        parameters: {

            type: "object",

            properties: {

                title: {
                    type: "string"
                },

                date: {
                    type: "string"
                },

                time: {
                    type: "string"
                }

            },

            required: [
                "title",
                "date",
                "time"
            ]
        }
    },


    {
        type: "function",

        name: "device_command",

        description:
            "Request a supported Android device operation.",

        parameters: {

            type: "object",

            properties: {

                command: {
                    type: "string",
                    description:
                        "A supported device operation."
                }

            },

            required: ["command"]
        }
    }

];


/*
========================================================
TOOL EXECUTION
========================================================
*/

async function executeTool(name, args) {

    console.log(
        "JARVIS TOOL:",
        name,
        args
    );


    switch (name) {

        case "open_app":

            return {
                success: true,
                action: "open_app",
                app: args.app,

                /*
                 Android client can execute this.
                */

                androidAction: {
                    type: "open_app",
                    app: args.app
                }
            };


        case "open_website":

            if (!args.url.startsWith("https://")) {

                return {
                    success: false,
                    error: "Only HTTPS URLs are allowed."
                };
            }

            return {
                success: true,
                action: "open_website",
                url: args.url
            };


        case "search_youtube":

            return {
                success: true,
                action: "search_youtube",
                query: args.query,

                url:
                    "https://www.youtube.com/results?search_query=" +
                    encodeURIComponent(args.query)
            };


        case "get_calendar_events":

            /*
             Replace this with Google Calendar API,
             Microsoft Graph, or your preferred
             calendar integration.
            */

            return {
                success: true,
                date: args.date,
                events: [],
                message:
                    "Calendar connector is ready for integration."
            };


        case "create_calendar_event":

            /*
             Replace with real calendar API.
            */

            return {
                success: true,
                title: args.title,
                date: args.date,
                time: args.time,

                message:
                    "Calendar event request accepted."
            };


        case "device_command":

            const allowedCommands = [

                "open_camera",
                "open_settings",
                "open_wifi_settings",
                "open_bluetooth_settings",
                "increase_volume",
                "decrease_volume"
            ];

            if (!allowedCommands.includes(args.command)) {

                return {
                    success: false,
                    error:
                        "Unsupported device command."
                };
            }

            return {
                success: true,
                command: args.command,

                androidAction: {
                    type: "device_command",
                    command: args.command
                }
            };


        default:

            return {
                success: false,
                error: "Unknown tool."
            };
    }
}


/*
========================================================
JARVIS ENDPOINT
========================================================
*/

app.post("/api/jarvis", async (req, res) => {

    try {

        const message = req.body?.message;

        if (!message) {

            return res.status(400).json({
                error: "Message is required."
            });
        }


        /*
        First model call.
        */

        let interaction =
            await ai.interactions.create({

                model: "gemini-3.8-flash",

                input: [
                    {
                        role: "user",
                        content: message
                    }
                ],

                system_instruction:
                    SYSTEM_INSTRUCTION,

                tools
            });


        /*
        Execute any requested function calls.
        */

        const functionResults = [];

        for (const step of interaction.steps || []) {

            if (step.type !== "function_call") {
                continue;
            }

            const result =
                await executeTool(
                    step.name,
                    step.arguments || {}
                );

            functionResults.push({

                type: "function_result",

                name: step.name,

                call_id: step.id,

                result: [
                    {
                        type: "text",
                        text: JSON.stringify(result)
                    }
                ]

            });
        }


        /*
        If tools were called, send their results
        back to Gemini for the final response.
        */

        if (functionResults.length > 0) {

            interaction =
                await ai.interactions.create({

                    model: "gemini-3.8-flash",

                    previous_interaction_id:
                        interaction.id,

                    input:
                        functionResults,

                    system_instruction:
                        SYSTEM_INSTRUCTION,

                    tools
                });
        }


        res.json({

            reply:
                interaction.output_text ||
                "Done.",

            interactionId:
                interaction.id

        });

    } catch (error) {

        console.error(error);

        res.status(500).json({

            error: "JARVIS backend error.",

            details:
                process.env.NODE_ENV === "development"
                    ? error.message
                    : undefined

        });
    }

});


/*
========================================================
HEALTH
========================================================
*/

app.get("/api/health", (req, res) => {

    res.json({
        status: "online",
        assistant: "JARVIS"
    });

});


app.listen(PORT, () => {

    console.log(
        `JARVIS server running on http://localhost:${PORT}`
    );

});
