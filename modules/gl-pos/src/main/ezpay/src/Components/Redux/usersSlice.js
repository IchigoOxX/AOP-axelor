import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import usersData from "../../data/users.json";
import {fetchCurrentUser, fetchUsers} from "../../Services/usersApi"; // Adjust the path to your JSON file

export const fetchUsersThunk = createAsyncThunk('users/fetchUsers', async (_, thunkAPI) => {
  try {
    const usersList = await fetchUsers();
    const currentUser = await fetchCurrentUser();
    return {usersList, currentUser};
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});

const initialState = {
  usersList: [],
  currentUser: null,
  loading: false,
  error: null,
};

const usersSlice = createSlice({
  name: "users",
  initialState,
  reducers: {
    selectUser(state, action) {
      state.currentUser = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
        .addCase(fetchUsersThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(fetchUsersThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.usersList = action.payload.usersList;
          state.currentUser = action.payload.currentUser;
        })
        .addCase(fetchUsersThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload;
        })
    ;
  },
});

export const { selectUser } = usersSlice.actions;
export default usersSlice.reducer;
