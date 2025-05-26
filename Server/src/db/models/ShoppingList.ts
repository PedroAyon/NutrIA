import { DataTypes } from 'sequelize';
import { sequelize } from '../index';
import { User } from './User';

export const ShoppingList = sequelize.define('ShoppingList', {
  id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true },
  userId: {
    type: DataTypes.STRING,
    references: { model: User, key: 'id' },
  },
  items: DataTypes.JSON,
});
