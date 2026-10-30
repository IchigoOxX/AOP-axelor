import { configureStore } from '@reduxjs/toolkit';
import productReducer from './productSlice';
import cartReducer from './cartSlice';
import customerReducer from './customerSlice';
import ticketReducer from './ticketSlice';
import paymentReducer from './paymentSlice';
import shiftReducer from './shiftSlice';
import userReducer from './usersSlice';
import {persistReducer, persistStore} from "redux-persist";

// const persistConfig = {
//   key: 'root',
//   storage
// };
//
// const persistedReducer = persistReducer(persistConfig, counterReducer);
// export const persistor = persistStore(store);
export const store = configureStore({
  reducer: {
    products: productReducer,
    cart: cartReducer,
    customers: customerReducer,
    tickets: ticketReducer,
    payment: paymentReducer,
    shifts: shiftReducer,
    users: userReducer,
  },
});
