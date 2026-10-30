import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import {
  addCustomer,
  deleteCustomer,
  deleteCustomerWithChildren,
  fetchCustomers,
  updateCustomer
} from '../../Services/customersApi';

const initialState = {
  customersList: [],
  loading: false,
  error: false,
  selectedCustomer: null,
};

export const fetchCustomersThunk = createAsyncThunk('customers/fetchCustomers', async (_, thunkAPI) => {
  try {
    return await fetchCustomers();
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const addCustomerThunk = createAsyncThunk('customers/addCustomer', async (customerData, thunkAPI) => {
  try {
    return await addCustomer(customerData);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const updateCustomerThunk = createAsyncThunk('customers/updateCustomer', async (customerData, thunkAPI) => {
  try {
    return await updateCustomer(customerData);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const deleteCustomerThunk = createAsyncThunk('customers/deleteCustomer', async (customerId, thunkAPI) => {
  try {
    await deleteCustomerWithChildren(customerId);
    return customerId
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

const customerSlice = createSlice({
  name: 'customers',
  initialState,
  reducers: {
    selectCustomer: (state, action) => {
      console.log("payload id: ", action.payload.id);
      // if (action.payload.id !== 131) {
        state.selectedCustomer = action.payload;
      // }
    },

  },
  extraReducers: builder => {
    builder
      .addCase(fetchCustomersThunk.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCustomersThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.customersList = action.payload;
      })
      .addCase(fetchCustomersThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to fetch customers';
      })
      .addCase(addCustomerThunk.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addCustomerThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.customersList.push(action.payload);
      })
      .addCase(addCustomerThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to add customer';
      })
      .addCase(updateCustomerThunk.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateCustomerThunk.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.customersList.findIndex(customer => customer.id === action.payload.id);
        if (index !== -1) {
          state.customersList[index] = action.payload;
        }
      })
      .addCase(updateCustomerThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to update customer';
      })
      .addCase(deleteCustomerThunk.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteCustomerThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.customersList = state.customersList.filter(customer => customer.id !== action.payload);
      })
      .addCase(deleteCustomerThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to delete customer';
      });
  },
});

export const { selectCustomer } = customerSlice.actions;
export default customerSlice.reducer;
