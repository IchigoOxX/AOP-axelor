import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import {getCurrentMonthAndDate} from "../../utils";

const initialState = {
  currentTicket: {},
  cartItems: [],
  total: 0,
  itemsCount: 0,
  orderNumber: null,
  orderId: null,
  loading: false,
  error: false
};
// const calculateTotal = cartItems => {
//   return cartItems.reduce((total, item) => {
//     const itemTotal =
//         parseFloat(item.product.salePrice) * parseInt(item.qty) * (1 - (parseFloat(item.product.discountPercentage) || 0) / 100);
//     return total + itemTotal;
//   }, 0);
// };
const calculateTotal = (cartItems) => {
  return cartItems.reduce((total, item) => {
    // Determine discount percentage
    const discountPercentage = (item.discountTypeSelect === 1 && item.discountAmount)
        ? parseFloat(item.discountAmount)
        : parseFloat(item.product.discountPercentage) || 0;
    const discountAmount = (item.discountTypeSelect === 2 && item.discountAmount)
        ? parseFloat(item.discountAmount)
        : parseFloat(item.product.discountAmount) || 0;
    console.log('discount amount: ', discountAmount)
    const itemTotal =
        (parseFloat(item.product.salePrice) - parseFloat((discountAmount))) *
        parseInt(item.qty) *
        (1 - discountPercentage / 100);

    return total + itemTotal;
  }, 0);
};
const calculateItemsCount = cartItems => {
  return cartItems.reduce((count, item) => {
    return count + parseInt(item.qty);
  }, 0);
};

// export const fetchlastOrderNumber = createAsyncThunk('saleOrders/getNextSaleOrderNum', async (_, thunkAPI) => {
//   try {
//     return await getNextSaleOrderNum();
//   } catch (error) {
//     return thunkAPI.rejectWithValue(error.message);
//   }
// });

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    addToCart: (state, action) => {
      const { product } = action.payload;
      const existingItem = state.cartItems.find(item => item.product.id === product.id);
      if (existingItem) {
        (existingItem.qty)++;
      } else {
        state.cartItems.push({ product, productName: product.name, qty: 1, isSelected: false });
      }
      // state.total += parseFloat(product.salePrice) * (1 - (parseFloat(product.discountPercentage) || 0) / 100);
      state.itemsCount++;
      state.total = calculateTotal(state.cartItems)
    },
    removeFromCart: (state, action) => {
      const { id } = action.payload;
      const indexToRemove = state.cartItems.findIndex(item => item.product.id === id);
      if (indexToRemove !== -1) {
        const itemToRemove = state.cartItems[indexToRemove];
        // state.total -=
        //     parseFloat(itemToRemove.product.salePrice) *
        //     (1 - (parseFloat(itemToRemove.product.discountPercentage) || 0) / 100) *
        //     itemToRemove.qty;
        state.itemsCount -= itemToRemove.qty;
        state.cartItems.splice(indexToRemove, 1);
        state.total = calculateTotal(state.cartItems)
      }
    },
    clearCart(state) {
      state.cartItems = [];
      state.total = 0;
      state.itemsCount = 0;
      state.currentTicket = {};
    },
    updateCurrentTicket: (state, action) => {
      state.currentTicket = action.payload;
    },
    updateCartItems: (state, action) => {
      state.cartItems = action.payload;
      state.total = calculateTotal(state.cartItems);
      state.itemsCount = calculateItemsCount(state.cartItems);
    },
    setOrderNumber(state, action) {
      const currentMonthAndDate = getCurrentMonthAndDate();
      const lastOrderNumber = action.payload;
      // Initialize orderNumber if not set or if date has changed
      if ( !lastOrderNumber || lastOrderNumber.slice(0, 5) !== `${currentMonthAndDate}-`) {
        state.orderNumber = `${currentMonthAndDate}-0001`;
      } else {
        // Increment the orderNumber, preserving the format
        const currentNumber = parseInt(lastOrderNumber.slice(-4), 10);
        const newNumber = String(currentNumber + 1).padStart(4, '0');
        state.orderNumber = `${currentMonthAndDate}-${newNumber}`;
      }
    },
    selectItem: (state, action) => {
      const { id } = action.payload;
      state.cartItems.forEach(item => {
        item.isSelected = item.product.id === id;
      });
    },
    unselectItem: state => {
      state.cartItems.forEach(item => {
        item.isSelected = false;
      });
    },
    incrementQuantity: (state, action) => {
      const { id } = action.payload;
      const itemToIncrement = state.cartItems.find(item => item.product.id === id);
      if (itemToIncrement) {
        itemToIncrement.qty++;
        // state.total +=
        //     parseFloat(itemToIncrement.product.salePrice) * (1 - (parseFloat(itemToIncrement.product.discountPercentage) || 0) / 100);
        state.itemsCount++;
        state.total = calculateTotal(state.cartItems);
      }
    },
    decrementQuantity: (state, action) => {
      const { id } = action.payload;
      const itemToDecrement = state.cartItems.find(item => item.product.id === id);
      if (itemToDecrement && itemToDecrement.qty > 1) {
        itemToDecrement.qty--;
        // state.total -=
        //     parseFloat(itemToDecrement.product.salePrice) * (1 - (parseFloat(itemToDecrement.product.discountPercentage) || 0) / 100);
        state.itemsCount--;
        state.total = calculateTotal(state.cartItems);
      } else {
        // state.total -=
        //     parseFloat(itemToDecrement.product.salePrice) *
        //     (1 - (parseFloat(itemToDecrement.product.discountPercentage) || 0) / 100) *
        //     itemToDecrement.qty;
        state.itemsCount--;
        state.cartItems = state.cartItems.filter(item => item.product.id !== id);
        state.total = calculateTotal(state.cartItems);
      }
    },
    setCartFromTicket: (state, action) => {
      const { ticket } = action.payload;
      state.cartItems = ticket.saleOrderLineList.map(line => ({
        product: line.product,
        productName: line.product.name,
        qty: line.qty,
        isSelected: line.isSelected || false
      }));
      state.total = ticket.total;
      state.itemsCount = ticket.itemsCount;
    },
  },
});

export const {
  addToCart,
  removeFromCart,
  clearCart,
  selectItem,
  unselectItem,
  incrementQuantity,
  decrementQuantity,
  updateCurrentTicket,
  updateCartItems,
  setOrderNumber,
  setCartFromTicket,
} = cartSlice.actions;

export default cartSlice.reducer;
