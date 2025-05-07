import { genkit } from 'genkit';
import { gemini20Flash, googleAI } from '@genkit-ai/googleai';
import dotenv from 'dotenv';

dotenv.config();

if (!process.env.GEMINI_API_KEY) {
    throw new Error('GEMINI_API_KEY environment variable is not set');
}

// configure a Genkit instance
const ai = genkit({
    plugins: [googleAI()],
    model: googleAI.model('gemini-2.0-flash'), // set default model
  });
  
  async function main() {
    // make a generation request
    const { text } = await ai.generate('Hello, Gemini!');
    console.log(text);
  }
  
  main();