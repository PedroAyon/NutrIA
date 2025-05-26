// src/db/sync.ts
import { sequelize } from './index';
import './models/User';
import './models/Recipe';
import './models/ShoppingList';

const syncDB = async () => {
  try {
    await sequelize.authenticate();
    await sequelize.sync({ alter: true });
    console.log("✅ Database synced successfully.");
  } catch (err) {
    console.error("❌ Failed to sync database:", err);
  }
};

export { syncDB };
