import { DataTypes } from 'sequelize';
import { sequelize } from '../index';

export const User = sequelize.define('User', {
  id: {
    type: DataTypes.STRING,
    primaryKey: true,
  },
});
