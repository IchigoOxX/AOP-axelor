import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { fetchCategories, fetchProducts } from '../../Services/productsApi';

const initialState = {
  products: {
    list: [],
    loading: false,
    error: null,
  },
  categories: {
    list: [],
    loading: false,
    error: null,
  },
};

export const fetchProductsThunk = createAsyncThunk('products/fetchProducts', async (_, thunkAPI) => {
  try {
    return await fetchProducts();
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

export const fetchCategoriesThunk = createAsyncThunk('categories/fetchCategories', async (_, thunkAPI) => {
  try {
    return await fetchCategories();
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {},
  extraReducers: builder => {
    builder
      // Products
      .addCase(fetchProductsThunk.pending, state => {
        state.products.loading = true;
        state.products.error = null;
      })
      .addCase(fetchProductsThunk.fulfilled, (state, action) => {
        state.products.loading = false;
        state.products.list = action.payload;
      })
      .addCase(fetchProductsThunk.rejected, (state, action) => {
        state.products.loading = false;
        state.products.error = action.payload || 'Failed to fetch products';
      })
      // Categories
      .addCase(fetchCategoriesThunk.pending, state => {
        state.categories.loading = true;
        state.categories.error = null;
      })
      .addCase(fetchCategoriesThunk.fulfilled, (state, action) => {
        state.categories.loading = false;
        state.categories.list = action.payload;
      })
      .addCase(fetchCategoriesThunk.rejected, (state, action) => {
        state.categories.loading = false;
        state.categories.error = action.payload || 'Failed to fetch categories';
      });
  },
});

export default productSlice.reducer;
