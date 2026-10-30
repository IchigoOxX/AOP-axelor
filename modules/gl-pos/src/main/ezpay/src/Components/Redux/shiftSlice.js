import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {addShift, fetchShifts, updateShift, updateShiftAfterSale} from "../../Services/shiftsApi";
import currentShift from "../Shifts/CurrentShift";

// Function to generate the next shift number based on existing shifts
const generateNextShiftNumber = (shifts) => {
  if (shifts.length === 0) {
    return "S1"; // If no shifts exist, start with S1
  } else {
    const lastShiftNumber = shifts[shifts.length - 1].shiftNumber;
    const lastNumber = parseInt(lastShiftNumber.slice(1)); // Extract number part and convert to integer
    return `S${lastNumber + 1}`; // Increment last number and format as new shift number
  }
};

export const fetchShiftsThunk = createAsyncThunk('shifts/fetchShifts', async (_, thunkAPI) => {
  try {
    const response = await fetchShifts();
    return response;
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

export const openShiftAsync = createAsyncThunk('shifts/openShift', async ({ startingCash, shiftOwner }, thunkAPI) => {
  try {
    const state = thunkAPI.getState();
    const newShiftNumber = generateNextShiftNumber(state.shifts.shifts);
    const now = new Date();
    const newShift = {
      shiftNumber: newShiftNumber,
      cashier: shiftOwner,
      shiftDate: now.toISOString().split('T')[0],
      openingHour: now.toISOString(),
      closingHour: null,
      status: 0,
      startingCash: startingCash || 0,
      // totalSales: 0,
      // totalRefunds: 0,
      // totalDiscounts: 0,
      cashBalance: startingCash || 0,
      // cashIn: 0,
      // cashOut: 0,
      // totalTransactions: 0,
      // numberOfSales: 0,
      // itemsSold: 0,
      // reference: null,
      // notes: null
    };
    return await addShift(newShift);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

export const updateShiftAsync = createAsyncThunk('shifts/updateShiftAsync', async (shiftData, thunkAPI) => {
  try {
    return await updateShift(shiftData);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const updateShiftAfterSaleThunk = createAsyncThunk('shifts/updateShiftAsync', async (data, thunkAPI) => {
  try {
    console.log('data1: ',data)
    return await updateShiftAfterSale(data);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const closeShiftAsync = createAsyncThunk('shifts/closeShiftAsync', async (_, thunkAPI) => {
  try {
    const state = thunkAPI.getState();
    const currentShift = state.shifts.currentShift;
    if (!currentShift) {
      throw new Error("No current shift to close");
    }
    const now = new Date();
    const updatedShift = {
      ...currentShift,
      status: 1,
      closingHour: now.toLocaleTimeString(),
    };

    return await updateShift(updatedShift);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

// Shift Slice
const shiftSlice = createSlice({
  name: "shifts",
  initialState: {
    shifts: [],
    currentShift: null,
    loading: false,
    error: false,
  },
  reducers: {
    setCurrentShift(state, action) {
      state.currentShift = action.payload;
    },
    updateShiftPayments(state, action) {
      if (state.currentShift) {
        const { amount } = action.payload;
        const currentShift = state.currentShift;
        currentShift.payments += amount;
        currentShift.totalExpectedCash = currentShift.startingCash + currentShift.payments - currentShift.refundsAmount;
      }
    },
  },
  extraReducers: (builder) => {
    builder
        .addCase(fetchShiftsThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(fetchShiftsThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.shifts = action.payload || [];
          state.currentShift = state.shifts?.find(shift => shift.status === 0) || null;
        })
        .addCase(fetchShiftsThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to fetch shifts';
        })
        .addCase(openShiftAsync.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(openShiftAsync.fulfilled, (state, action) => {
          state.loading = false;
          state.shifts.push(action.payload);
          state.currentShift = action.payload;
        })
        .addCase(openShiftAsync.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to open shift';
        })
        .addCase(closeShiftAsync.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(closeShiftAsync.fulfilled, (state, action) => {
          state.loading = false;
          const index = state.shifts.findIndex(shift => shift.id === action.payload.id);
          if (index !== -1) {
            state.shifts[index] = action.payload;
          }
          state.currentShift = null;
        })
        .addCase(closeShiftAsync.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to close shift';
        })
        .addCase(updateShiftAfterSaleThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(updateShiftAfterSaleThunk.fulfilled, (state, action) => {
          state.loading = false;
          const index = state.shifts.findIndex(shift => shift.id === action.payload);
          if (index !== -1) {
            state.shifts[index] = action.payload;
          }
        })
        .addCase(updateShiftAfterSaleThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to close shift';
        });

  },
});

export const { setCurrentShift, updateShiftPayments } = shiftSlice.actions;

export default shiftSlice.reducer;
