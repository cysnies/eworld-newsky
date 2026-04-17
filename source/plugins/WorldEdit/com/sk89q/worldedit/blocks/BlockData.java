package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.CuboidClipboard;

public final class BlockData {
   public static int rotate90(int type, int data) {
      switch (type) {
         case 17:
            if (data >= 4 && data <= 11) {
               data ^= 12;
            }
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 24:
         case 25:
         case 26:
         case 30:
         case 31:
         case 32:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 52:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 70:
         case 72:
         case 73:
         case 74:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 87:
         case 88:
         case 89:
         case 90:
         case 92:
         case 95:
         case 97:
         case 98:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 110:
         case 111:
         case 112:
         case 113:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 129:
         case 132:
         case 133:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 144:
         case 147:
         case 148:
         case 151:
         case 152:
         case 153:
         case 155:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         default:
            break;
         case 23:
         case 158:
            int dispPower = data & 8;
            switch (data & -9) {
               case 2:
                  return 5 | dispPower;
               case 3:
                  return 4 | dispPower;
               case 4:
                  return 2 | dispPower;
               case 5:
                  return 3 | dispPower;
               default:
                  return data;
            }
         case 29:
         case 33:
         case 34:
            int rest = data & -8;
            switch (data & 7) {
               case 2:
                  return 5 | rest;
               case 3:
                  return 4 | rest;
               case 4:
                  return 2 | rest;
               case 5:
                  return 3 | rest;
               default:
                  return data;
            }
         case 50:
         case 75:
         case 76:
            switch (data) {
               case 1:
                  return 3;
               case 2:
                  return 4;
               case 3:
                  return 2;
               case 4:
                  return 1;
               default:
                  return data;
            }
         case 53:
         case 67:
         case 108:
         case 109:
         case 114:
         case 128:
         case 134:
         case 135:
         case 136:
         case 156:
            switch (data) {
               case 0:
                  return 2;
               case 1:
                  return 3;
               case 2:
                  return 1;
               case 3:
                  return 0;
               case 4:
                  return 6;
               case 5:
                  return 7;
               case 6:
                  return 5;
               case 7:
                  return 4;
               default:
                  return data;
            }
         case 54:
         case 61:
         case 62:
         case 65:
         case 68:
         case 130:
         case 146:
         case 154:
            switch (data) {
               case 2:
                  return 5;
               case 3:
                  return 4;
               case 4:
                  return 2;
               case 5:
                  return 3;
               default:
                  return data;
            }
         case 63:
            return (data + 4) % 16;
         case 64:
         case 71:
         case 127:
         case 131:
            int extra = data & -4;
            int withoutFlags = data & 3;
            switch (withoutFlags) {
               case 0:
                  return 1 | extra;
               case 1:
                  return 2 | extra;
               case 2:
                  return 3 | extra;
               case 3:
                  return 0 | extra;
               default:
                  return data;
            }
         case 66:
            switch (data) {
               case 6:
                  return 7;
               case 7:
                  return 8;
               case 8:
                  return 9;
               case 9:
                  return 6;
            }
         case 27:
         case 28:
         case 157:
            switch (data & 7) {
               case 0:
                  return 1 | data & -8;
               case 1:
                  return 0 | data & -8;
               case 2:
                  return 5 | data & -8;
               case 3:
                  return 4 | data & -8;
               case 4:
                  return 2 | data & -8;
               case 5:
                  return 3 | data & -8;
               default:
                  return data;
            }
         case 69:
         case 77:
         case 143:
            int thrown = data & 8;
            int withoutThrown = data & -9;
            switch (withoutThrown) {
               case 1:
                  return 3 | thrown;
               case 2:
                  return 4 | thrown;
               case 3:
                  return 2 | thrown;
               case 4:
                  return 1 | thrown;
               default:
                  return data;
            }
         case 86:
         case 91:
            switch (data) {
               case 0:
                  return 1;
               case 1:
                  return 2;
               case 2:
                  return 3;
               case 3:
                  return 0;
               default:
                  return data;
            }
         case 93:
         case 94:
         case 149:
         case 150:
            int dir = data & 3;
            int delay = data - dir;
            switch (dir) {
               case 0:
                  return 1 | delay;
               case 1:
                  return 2 | delay;
               case 2:
                  return 3 | delay;
               case 3:
                  return 0 | delay;
               default:
                  return data;
            }
         case 96:
            int withoutOrientation = data & -4;
            int orientation = data & 3;
            switch (orientation) {
               case 0:
                  return 3 | withoutOrientation;
               case 1:
                  return 2 | withoutOrientation;
               case 2:
                  return 0 | withoutOrientation;
               case 3:
                  return 1 | withoutOrientation;
               default:
                  return data;
            }
         case 99:
         case 100:
            if (data >= 10) {
               return data;
            }

            return data * 3 % 10;
         case 106:
            return (data << 1 | data >> 3) & 15;
         case 107:
            return data + 1 & 3 | data & -4;
         case 145:
            return data ^ 1;
         case 170:
            if (data == 4) {
               return 8;
            }

            if (data == 8) {
               return 4;
            }

            return 0;
      }

      return data;
   }

   public static int rotate90Reverse(int type, int data) {
      switch (type) {
         case 17:
            if (data >= 4 && data <= 11) {
               data ^= 12;
            }
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 24:
         case 25:
         case 26:
         case 30:
         case 31:
         case 32:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 52:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 70:
         case 72:
         case 73:
         case 74:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 87:
         case 88:
         case 89:
         case 90:
         case 92:
         case 95:
         case 97:
         case 98:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 110:
         case 111:
         case 112:
         case 113:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 129:
         case 132:
         case 133:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 144:
         case 147:
         case 148:
         case 151:
         case 152:
         case 153:
         case 155:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         default:
            break;
         case 23:
         case 158:
            int dispPower = data & 8;
            switch (data & -9) {
               case 2:
                  return 4 | dispPower;
               case 3:
                  return 5 | dispPower;
               case 4:
                  return 3 | dispPower;
               case 5:
                  return 2 | dispPower;
               default:
                  return data;
            }
         case 50:
         case 75:
         case 76:
            switch (data) {
               case 1:
                  return 4;
               case 2:
                  return 3;
               case 3:
                  return 1;
               case 4:
                  return 2;
               default:
                  return data;
            }
         case 53:
         case 67:
         case 108:
         case 109:
         case 114:
         case 128:
         case 134:
         case 135:
         case 136:
         case 156:
            switch (data) {
               case 0:
                  return 3;
               case 1:
                  return 2;
               case 2:
                  return 0;
               case 3:
                  return 1;
               case 4:
                  return 7;
               case 5:
                  return 6;
               case 6:
                  return 4;
               case 7:
                  return 5;
               default:
                  return data;
            }
         case 54:
         case 61:
         case 62:
         case 65:
         case 68:
         case 130:
         case 146:
         case 154:
            switch (data) {
               case 2:
                  return 4;
               case 3:
                  return 5;
               case 4:
                  return 3;
               case 5:
                  return 2;
               default:
                  return data;
            }
         case 63:
            return (data + 12) % 16;
         case 64:
         case 71:
         case 127:
         case 131:
            int extra = data & -4;
            int withoutFlags = data & 3;
            switch (withoutFlags) {
               case 0:
                  return 3 | extra;
               case 1:
                  return 0 | extra;
               case 2:
                  return 1 | extra;
               case 3:
                  return 2 | extra;
               default:
                  return data;
            }
         case 66:
            switch (data) {
               case 6:
                  return 9;
               case 7:
                  return 6;
               case 8:
                  return 7;
               case 9:
                  return 8;
            }
         case 27:
         case 28:
         case 157:
            int power = data & -8;
            switch (data & 7) {
               case 0:
                  return 1 | power;
               case 1:
                  return 0 | power;
               case 2:
                  return 4 | power;
               case 3:
                  return 5 | power;
               case 4:
                  return 3 | power;
               case 5:
                  return 2 | power;
               default:
                  return data;
            }
         case 69:
         case 77:
         case 143:
            int thrown = data & 8;
            int withoutThrown = data & -9;
            switch (withoutThrown) {
               case 1:
                  return 4 | thrown;
               case 2:
                  return 3 | thrown;
               case 3:
                  return 1 | thrown;
               case 4:
                  return 2 | thrown;
               default:
                  return data;
            }
         case 86:
         case 91:
            switch (data) {
               case 0:
                  return 3;
               case 1:
                  return 0;
               case 2:
                  return 1;
               case 3:
                  return 2;
               default:
                  return data;
            }
         case 93:
         case 94:
         case 149:
         case 150:
            int dir = data & 3;
            int delay = data - dir;
            switch (dir) {
               case 0:
                  return 3 | delay;
               case 1:
                  return 0 | delay;
               case 2:
                  return 1 | delay;
               case 3:
                  return 2 | delay;
               default:
                  return data;
            }
         case 96:
            int withoutOrientation = data & -4;
            int orientation = data & 3;
            switch (orientation) {
               case 0:
                  return 2 | withoutOrientation;
               case 1:
                  return 3 | withoutOrientation;
               case 2:
                  return 1 | withoutOrientation;
               case 3:
                  return 0 | withoutOrientation;
            }
         case 29:
         case 33:
         case 34:
            int rest = data & -8;
            switch (data & 7) {
               case 2:
                  return 4 | rest;
               case 3:
                  return 5 | rest;
               case 4:
                  return 3 | rest;
               case 5:
                  return 2 | rest;
               default:
                  return data;
            }
         case 99:
         case 100:
            if (data >= 10) {
               return data;
            }

            return data * 7 % 10;
         case 106:
            return (data >> 1 | data << 3) & 15;
         case 107:
            return data + 3 & 3 | data & -4;
         case 145:
            return data ^ 1;
         case 170:
            if (data == 4) {
               return 8;
            }

            if (data == 8) {
               return 4;
            }

            return 0;
      }

      return data;
   }

   public static int flip(int type, int data) {
      return rotate90(type, rotate90(type, data));
   }

   public static int flip(int type, int data, CuboidClipboard.FlipDirection direction) {
      int flipX = 0;
      int flipY = 0;
      int flipZ = 0;
      switch (direction) {
         case NORTH_SOUTH:
            flipZ = 1;
            break;
         case WEST_EAST:
            flipX = 1;
            break;
         case UP_DOWN:
            flipY = 1;
      }

      switch (type) {
         case 23:
         case 158:
            int dispPower = data & 8;
            switch (data & -9) {
               case 0:
               case 1:
                  return data ^ flipY | dispPower;
               case 2:
               case 3:
                  return data ^ flipZ | dispPower;
               case 4:
               case 5:
                  return data ^ flipX | dispPower;
            }
         case 24:
         case 25:
         case 26:
         case 30:
         case 31:
         case 32:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 52:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 70:
         case 72:
         case 73:
         case 74:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 87:
         case 88:
         case 89:
         case 90:
         case 92:
         case 95:
         case 97:
         case 98:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 110:
         case 111:
         case 112:
         case 113:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 129:
         case 132:
         case 133:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 144:
         case 145:
         case 147:
         case 148:
         case 151:
         case 152:
         case 153:
         case 155:
         default:
            break;
         case 29:
         case 33:
         case 34:
            switch (data & -9) {
               case 0:
               case 1:
                  return data ^ flipY;
               case 2:
               case 3:
                  return data ^ flipZ;
               case 4:
               case 5:
                  return data ^ flipX;
               default:
                  return data;
            }
         case 44:
         case 126:
            return data ^ flipY << 3;
         case 50:
         case 75:
         case 76:
            if (data > 4) {
               break;
            }
         case 69:
         case 77:
         case 143:
            switch (data & -9) {
               case 1:
                  return data + flipX;
               case 2:
                  return data - flipX;
               case 3:
                  return data + flipZ;
               case 4:
                  return data - flipZ;
               default:
                  return data;
            }
         case 53:
         case 67:
         case 108:
         case 109:
         case 114:
         case 128:
         case 134:
         case 135:
         case 136:
         case 156:
            data ^= flipY << 2;
            switch (data) {
               case 0:
               case 1:
               case 4:
               case 5:
                  return data ^ flipX;
               case 2:
               case 3:
               case 6:
               case 7:
                  return data ^ flipZ;
               default:
                  return data;
            }
         case 54:
         case 61:
         case 62:
         case 65:
         case 68:
         case 130:
         case 146:
         case 154:
            switch (data) {
               case 2:
               case 3:
                  return data ^ flipZ;
               case 4:
               case 5:
                  return data ^ flipX;
               default:
                  return data;
            }
         case 63:
            switch (direction) {
               case NORTH_SOUTH:
                  return 16 - data & 15;
               case WEST_EAST:
                  return 8 - data & 15;
               default:
                  return data;
            }
         case 64:
         case 71:
            data ^= flipY << 3;
            switch (data & 3) {
               case 0:
                  return data + flipX + flipZ * 3;
               case 1:
                  return data - flipX + flipZ;
               case 2:
                  return data + flipX - flipZ;
               case 3:
                  return data - flipX - flipZ * 3;
               default:
                  return data;
            }
         case 66:
            switch (data) {
               case 6:
                  return data + flipX + flipZ * 3;
               case 7:
                  return data - flipX + flipZ;
               case 8:
                  return data + flipX - flipZ;
               case 9:
                  return data - flipX - flipZ * 3;
            }
         case 27:
         case 28:
         case 157:
            switch (data & 7) {
               case 0:
               case 1:
                  return data;
               case 2:
               case 3:
                  return data ^ flipX;
               case 4:
               case 5:
                  return data ^ flipZ;
               default:
                  return data;
            }
         case 86:
         case 91:
            if (data > 3) {
               break;
            }
         case 93:
         case 94:
         case 127:
         case 131:
         case 149:
         case 150:
            switch (data & 3) {
               case 0:
               case 2:
                  return data ^ flipZ << 1;
               case 1:
               case 3:
                  return data ^ flipX << 1;
               default:
                  return data;
            }
         case 96:
            switch (data & 3) {
               case 0:
               case 1:
                  return data ^ flipZ;
               case 2:
               case 3:
                  return data ^ flipX;
               default:
                  return data;
            }
         case 99:
         case 100:
            switch (data) {
               case 1:
               case 4:
               case 7:
                  data += flipX * 2;
               case 2:
               case 5:
               case 8:
               default:
                  break;
               case 3:
               case 6:
               case 9:
                  data -= flipX * 2;
            }

            switch (data) {
               case 1:
               case 2:
               case 3:
                  return data + flipZ * 6;
               case 4:
               case 5:
               case 6:
               default:
                  return data;
               case 7:
               case 8:
               case 9:
                  return data - flipZ * 6;
            }
         case 106:
            int bit1;
            int bit2;
            switch (direction) {
               case NORTH_SOUTH:
                  bit1 = 2;
                  bit2 = 8;
                  break;
               case WEST_EAST:
                  bit1 = 1;
                  bit2 = 4;
                  break;
               default:
                  return data;
            }

            int newData = data & ~(bit1 | bit2);
            if ((data & bit1) != 0) {
               newData |= bit2;
            }

            if ((data & bit2) != 0) {
               newData |= bit1;
            }

            return newData;
         case 107:
            switch (data & 3) {
               case 0:
               case 2:
                  return data ^ flipZ * 2;
               case 1:
               case 3:
                  return data ^ flipX * 2;
            }
      }

      return data;
   }

   public static int cycle(int type, int data, int increment) {
      if (increment != -1 && increment != 1) {
         throw new IllegalArgumentException("Increment must be 1 or -1.");
      } else {
         switch (type) {
            case 6:
               if ((data & 3) != 3 && data <= 15) {
                  int var8 = data & -4;
                  return mod((data & 3) + increment, 3) | var8;
               }

               return -1;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 19:
            case 20:
            case 21:
            case 22:
            case 25:
            case 26:
            case 27:
            case 28:
            case 30:
            case 32:
            case 34:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 51:
            case 52:
            case 55:
            case 56:
            case 57:
            case 58:
            case 64:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 77:
            case 79:
            case 80:
            case 82:
            case 84:
            case 85:
            case 87:
            case 88:
            case 89:
            case 90:
            case 95:
            case 101:
            case 102:
            case 103:
            case 110:
            case 111:
            case 112:
            case 113:
            case 116:
            case 117:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 129:
            case 131:
            case 132:
            case 133:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 147:
            case 148:
            case 151:
            case 152:
            case 153:
            case 157:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 167:
            case 168:
            case 169:
            default:
               return -1;
            case 17:
               if (increment == -1) {
                  int store = data & 3;
                  return mod((data & -4) + 4, 16) | store;
               }

               int store = data & -4;
               return mod((data & 3) + 1, 4) | store;
            case 18:
            case 93:
            case 94:
            case 96:
            case 107:
            case 149:
            case 150:
               if (data > 7) {
                  return -1;
               }

               int store = data & -4;
               return mod((data & 3) + increment, 4) | store;
            case 23:
            case 158:
               int store = data & 8;
               data &= -9;
               if (data > 5) {
                  return -1;
               }

               return mod(data + increment, 6) | store;
            case 24:
            case 31:
            case 97:
               if (data > 2) {
                  return -1;
               }

               return mod(data + increment, 3);
            case 29:
            case 33:
            case 43:
            case 44:
            case 92:
               if (data > 5) {
                  return -1;
               }

               return mod(data + increment, 6);
            case 35:
            case 159:
            case 171:
               if (increment == 1) {
                  data = nextClothColor(data);
               } else if (increment == -1) {
                  data = prevClothColor(data);
               }

               return data;
            case 50:
            case 75:
            case 76:
               if (data >= 1 && data <= 4) {
                  return mod(data - 1 + increment, 4) + 1;
               }

               return -1;
            case 53:
            case 67:
            case 108:
            case 109:
            case 114:
            case 128:
            case 134:
            case 135:
            case 136:
            case 156:
               if (data > 7) {
                  return -1;
               }

               return mod(data + increment, 8);
            case 54:
            case 61:
            case 62:
            case 65:
            case 68:
            case 130:
            case 146:
            case 154:
               if (data >= 2 && data <= 5) {
                  return mod(data - 2 + increment, 4) + 2;
               }

               return -1;
            case 59:
            case 104:
            case 105:
               if (data > 6) {
                  return -1;
               }

               return mod(data + increment, 7);
            case 60:
               if (data > 8) {
                  return -1;
               }

               return mod(data + increment, 9);
            case 63:
            case 78:
            case 81:
            case 83:
            case 106:
            case 127:
               if (data > 15) {
                  return -1;
               }

               return mod(data + increment, 16);
            case 66:
               if (data >= 6 && data <= 9) {
                  return mod(data - 6 + increment, 4) + 6;
               }

               return -1;
            case 86:
            case 91:
            case 98:
            case 115:
            case 118:
            case 125:
            case 126:
            case 155:
            case 170:
               if (data > 3) {
                  return -1;
               }

               return mod(data + increment, 4);
            case 99:
            case 100:
               return data > 10 ? -1 : mod(data + increment, 11);
         }
      }
   }

   private static int mod(int x, int y) {
      int res = x % y;
      return res < 0 ? res + y : res;
   }

   public static int nextClothColor(int data) {
      switch (data) {
         case 0:
            return 8;
         case 1:
            return 4;
         case 2:
            return 6;
         case 3:
            return 11;
         case 4:
            return 5;
         case 5:
            return 13;
         case 6:
            return 0;
         case 7:
            return 15;
         case 8:
            return 7;
         case 9:
            return 3;
         case 10:
            return 2;
         case 11:
            return 10;
         case 12:
            return 14;
         case 13:
            return 9;
         case 14:
            return 1;
         case 15:
            return 12;
         default:
            return 0;
      }
   }

   public static int prevClothColor(int data) {
      switch (data) {
         case 0:
            return 6;
         case 1:
            return 14;
         case 2:
            return 10;
         case 3:
            return 9;
         case 4:
            return 1;
         case 5:
            return 4;
         case 6:
            return 2;
         case 7:
            return 8;
         case 8:
            return 0;
         case 9:
            return 13;
         case 10:
            return 11;
         case 11:
            return 3;
         case 12:
            return 15;
         case 13:
            return 5;
         case 14:
            return 12;
         case 15:
            return 7;
         default:
            return 0;
      }
   }
}
