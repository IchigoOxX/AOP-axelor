import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import {updateShift} from "../../Services/shiftsApi";
import {fetchShiftsThunk} from "./shiftSlice";
import {addNewPayment, fetchPaymentModes} from "../../Services/paymentApi";

export const fetchPaymentModesThunk = createAsyncThunk('payments/fetchPaymentModes', async (_, thunkAPI) => {
  try {
    return await fetchPaymentModes();
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const addPaymentThunk = createAsyncThunk('payments/addPaymentAsync', async (paymentData, thunkAPI) => {
  try {
    const state = thunkAPI.getState();
    paymentData.user = state.users.currentUser;
    return await addNewPayment(paymentData);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const paymentSlice = createSlice({
  name: 'payment',
  initialState: {
    total: 0,
    paidAmount: 0,
    remainingAmount: 0,
    changeAmount: 0,
    paymentModes: [],
    payments: [],
    loading: false,
    error: false
  },
  reducers: {
    addPayment: (state, action) => {
      const payment = action.payload;
        console.log("payment: ", payment)
      // Find existing payment mode
      const existingPaymentIndex = state.payments.findIndex(p => p.mode.id === payment.paymentMode.id);

      if (existingPaymentIndex !== -1) {
        // Update the existing payment's isSelected state and amount
        state.payments[existingPaymentIndex].isSelected = true;

        if (payment.paidAmount !== 0) {
          state.payments[existingPaymentIndex].payment.paidAmount = payment.paidAmount;
        }
      } else {
        // Add new payment if it doesn't exist
        state.payments.push({...payment, isSelected: true});
      }

      // Deselect all other payments
      state.payments.forEach((p, index) => {
        console.log("p: ", p)
        if (index !== existingPaymentIndex && p?.paymentMode?.id !== payment.paymentMode.id) {
          p.isSelected = false;
        }
      });

      // Recalculate amounts
      state.paidAmount = state.payments.reduce((sum, payment) => sum + payment.amount, 0);
      state.remainingAmount = state.total - state.paidAmount;
      state.changeAmount = state.paidAmount > state.total ? state.paidAmount - state.total : 0;
    },
    removePayment: (state, action) => {
      const index = action.payload;
      if (index >= 0 && index < state.payments.length) {
        const removedAmount = state.payments[index].amount;
        state.payments.splice(index, 1);
        state.paidAmount -= removedAmount;
        state.remainingAmount = state.paidAmount < state.total ? state.total - state.paidAmount : 0;
        state.changeAmount = state.paidAmount > state.total ? state.paidAmount - state.total : 0;
      }
    },
    setTotal: (state, action) => {
      state.total = action.payload;
      state.remainingAmount = action.payload - state.paidAmount;
    },
    updatePaymentAmount: (state, action) => {
      const { mode, amount } = action.payload;
      const selectedPayment = state.payments.find(payment => payment.paymentMode === mode && payment.isSelected);
      if (selectedPayment) {
        selectedPayment.amount = amount;
        state.paidAmount = state.payments.reduce((sum, payment) => sum + payment.amount, 0);
        state.remainingAmount = state.paidAmount < state.total ? state.total - state.paidAmount : 0;
        state.changeAmount = state.paidAmount > state.total ? state.paidAmount - state.total : 0;
      }
    },
    selectPaymentMode: (state, action) => {
      const mode = action.payload;

      state.payments.forEach(payment => {
        payment.isSelected = payment.paymentMode === mode;
      });
    },
    resetPayments: state => {
      state.payments = [];
      state.paidAmount = 0;
      state.remainingAmount = state.total;
      state.changeAmount = 0;
    },
  },
  extraReducers: (builder) => {
    builder
        .addCase(fetchPaymentModesThunk.pending, state => {
          state.loading = true;
          state.error = null;
        })
        .addCase(fetchPaymentModesThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.paymentModes = action.payload;
        })
        .addCase(fetchPaymentModesThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to fetch payment modes';
        })
        .addCase(addPaymentThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(addPaymentThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.shifts = action.payload || [];
          state.currentShift = state.shifts?.find(shift => shift.status === 0) || null;
        })
        .addCase(addPaymentThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to add payment';
        });
  }
});

export const { addPayment, removePayment, setTotal, updatePaymentAmount, selectPaymentMode, resetPayments } =
  paymentSlice.actions;

export default paymentSlice.reducer;
