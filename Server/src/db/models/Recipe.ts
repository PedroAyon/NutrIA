import { DataTypes } from 'sequelize';
import { sequelize } from '../index';
import { User } from './User';

export const Recipe = sequelize.define('Recipe', {
  id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true },
  userId: {
    type: DataTypes.STRING,
    references: { model: User, key: 'id' },
  },
  name: DataTypes.STRING,
  description: DataTypes.TEXT,
  ingredients: DataTypes.JSON,
  instructions: DataTypes.JSON,
  prepTime: DataTypes.STRING,
  calories: DataTypes.INTEGER,
});
