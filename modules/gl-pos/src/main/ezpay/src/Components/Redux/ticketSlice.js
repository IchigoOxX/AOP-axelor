import {createAsyncThunk, createSelector, createSlice} from '@reduxjs/toolkit';
import ticketsData from '../../data/tickets.json';
import {
  addSaleOrder, comfirmSaleOrder,
  completeSaleOrder,
  deleteSaleOrder, fetchSaleOrderById,
  fetchSaleOrders,
  updateSaleOrderFields
} from "../../Services/SaleOrderApi";
import {deleteCustomerWithChildren} from "../../Services/customersApi";
import {deleteCustomerThunk} from "./customerSlice";

const initialState = {
  tickets: [],
  // openTickets: [],
  loading: false,
  error: false,
}

export const fetchSaleOrdersThunk = createAsyncThunk('saleOrders/fetchSaleOrders', async (_, thunkAPI) => {
  try {
    return await fetchSaleOrders();
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const fetchSaleOrdersByIdThunk = createAsyncThunk('saleOrders/fetchSaleOrderById', async (saleOrderId, thunkAPI) => {
  try {
    return await fetchSaleOrderById(saleOrderId);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const createTicketThunk = createAsyncThunk('tickets/createTicket', async (ticketData, thunkAPI) => {
  try {
    console.log('ticket data: ', ticketData)
    const state = thunkAPI.getState();
    const activeCompany = state.users.currentUser.activeCompany;
    if (activeCompany) {
      ticketData.company = activeCompany;
    }
    ticketData.creationDate = new Date();
    if (!ticketData.clientPartner) {
      ticketData.clientPartner = { id: 131, fullName: "T0119 - Unknown" };
    }
    console.log("ticket data:", ticketData)
    return await addSaleOrder(ticketData);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const updateTicketThunk = createAsyncThunk('tickets/updateTicket', async(ticketData, thunkAPI) => {
  try {
    console.log('update ticket data: ', ticketData);
    return await updateSaleOrderFields(ticketData);
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const deleteTicketThunk = createAsyncThunk('tickets/deleteTicket', async (ticketId, thunkAPI) => {
  try {
    await deleteSaleOrder(ticketId);
    return ticketId;
  } catch (error) {
    return thunkAPI.rejectWithValue(error.message);
  }
});
export const confirmSaleOrderThunk = createAsyncThunk('tickets/confirmSaleOrder', async (saleOrderId, thunkAPI) => {
      try {
        return await comfirmSaleOrder(saleOrderId);
      } catch (error) {
        return thunkAPI.rejectWithValue(error.message);
      }
    }
);
export const completeSaleOrderThunk = createAsyncThunk('tickets/completeSaleOrder', async (saleOrderId, thunkAPI) => {
      try {
        return await completeSaleOrder(saleOrderId);
      } catch (error) {
        return thunkAPI.rejectWithValue(error.message);
      }
    }
);

const ticketSlice = createSlice({
  name: 'tickets',
  initialState,
  reducers: {
    saveTicket: (state, action) => {
      const { saleOrderSeq, clientPartner, total, itemsCount, saleOrderLineList, status } = action.payload;
      const itemStatus = status === 'open' ? 'pending' : 'sold';
      const existingTicketIndex = state.tickets.findIndex(ticket => ticket.saleOrderSeq === saleOrderSeq);
      if (existingTicketIndex !== -1) {
        state.tickets[existingTicketIndex] = {
          ...state.tickets[existingTicketIndex],
          clientPartner,
          total,
          itemsCount,
          saleOrderLineList: saleOrderLineList.map(item => ({
            ...item,
            status: itemStatus,
          })),
          status,
        };
      } else {
        const newTicket = {
          saleOrderSeq,
          clientPartner,
          total,
          itemsCount,
          saleOrderLineList: saleOrderLineList.map(item => ({
            ...item,
            status: itemStatus,
          })),
          creationDate: new Date(),
          status,
        };
        state.tickets.push(newTicket);
      }
    },
    updateTicketStatus: (state, action) => {
      const { saleOrderSeq, newStatus } = action.payload;
      const ticketToUpdate = state.tickets.find(ticket => ticket.saleOrderSeq === saleOrderSeq);
      if (ticketToUpdate) {
        ticketToUpdate.status = newStatus;
        const newItemStatus = newStatus === 'open' ? 'pending' : 'sold';
        ticketToUpdate.saleOrderLineList = ticketToUpdate.saleOrderLineList.map(item => ({
          ...item,
          status: newItemStatus === 'sold' && item.status === 'pending' ? 'sold' : item.status,
        }));
      }
    },
    removeTicket: (state, action) => {
      const saleOrderSeq = action.payload;
      state.tickets = state.tickets.filter(ticket => ticket.saleOrderSeq !== saleOrderSeq);
    },
    refundItems: (state, action) => {
      const { saleOrderSeq, refundedItems } = action.payload;
      const ticketIndex = state.tickets.findIndex(ticket => ticket.saleOrderSeq === saleOrderSeq);

      if (ticketIndex !== -1) {
        const ticket = state.tickets[ticketIndex];
        const processedItems = new Set();

        refundedItems.forEach(refundItem => {
          if (!processedItems.has(refundItem.id)) {
            processedItems.add(refundItem.id);

            const itemIndex = ticket.saleOrderLineList.findIndex(item => item.id === refundItem.id);
            const item = ticket.saleOrderLineList[itemIndex];

            if (refundItem.selected) {
              if (refundItem.status === 'sold') {
                const refundItemInListIndex = ticket.saleOrderLineList.findIndex(
                  item => item.id === refundItem.id && item.status === 'refund',
                );

                if (refundItemInListIndex !== -1) {
                  const maxRefundableQuantity = item.quantity + ticket.saleOrderLineList[refundItemInListIndex].quantity;
                  ticket.saleOrderLineList[refundItemInListIndex].quantity = Math.min(
                    refundItem.quantity,
                    maxRefundableQuantity,
                  );
                } else {
                  ticket.saleOrderLineList.push({
                    ...item,
                    quantity: Math.min(refundItem.quantity, item.quantity),
                    status: 'refund',
                  });
                }

                item.quantity -= Math.min(refundItem.quantity, item.quantity);

                if (item.quantity <= 0) {
                  ticket.saleOrderLineList.splice(itemIndex, 1);
                }
              } else if (refundItem.status === 'refund') {
                const refundItemInListIndex = ticket.saleOrderLineList.findIndex(
                  item => item.id === refundItem.id && item.status === 'refund',
                );

                if (refundItemInListIndex !== -1) {
                  const diff = refundItem.quantity - ticket.saleOrderLineList[refundItemInListIndex].quantity;
                  ticket.saleOrderLineList[refundItemInListIndex].quantity = refundItem.quantity;

                  if (diff > 0) {
                    item.quantity -= diff;
                    if (item.quantity <= 0) {
                      ticket.saleOrderLineList.splice(itemIndex, 1);
                    }
                  } else if (diff < 0) {
                    const absDiff = Math.abs(diff);
                    item.quantity += absDiff;
                  }

                  if (ticket.saleOrderLineList[refundItemInListIndex].quantity <= 0) {
                    ticket.saleOrderLineList.splice(refundItemInListIndex, 1);
                  }
                }
              }
            } else {
              // Logic for when selected is set to false and status is refund
              if (refundItem.status === 'refund') {
                const refundItemInListIndex = ticket.saleOrderLineList.findIndex(
                  item => item.id === refundItem.id && item.status === 'refund',
                );

                if (refundItemInListIndex !== -1) {
                  const refundItemInList = ticket.saleOrderLineList[refundItemInListIndex];
                  refundItemInList.quantity -= refundItem.quantity;

                  if (refundItemInList.quantity <= 0) {
                    ticket.saleOrderLineList.splice(refundItemInListIndex, 1);
                  }

                  const soldItemIndex = ticket.saleOrderLineList.findIndex(
                    item => item.id === refundItem.id && item.status === 'sold',
                  );

                  if (soldItemIndex !== -1) {
                    ticket.saleOrderLineList[soldItemIndex].quantity += refundItem.quantity;
                  } else {
                    ticket.saleOrderLineList.push({
                      ...item,
                      quantity: refundItem.quantity,
                      status: 'sold',
                    });
                  }
                }
              }
            }
          }
        });
      }
    },
  },
  extraReducers: builder => {
    builder
        .addCase(fetchSaleOrdersThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(fetchSaleOrdersThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.tickets = action.payload;
          // state.tickets = state.tickets.filter(ticket => ticket.statusSelected === 1);
        })
        .addCase(fetchSaleOrdersThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to fetch sale orders';
        })
        // .addCase(fetchSaleOrdersByIdThunk.pending, (state) => {
        //   state.loading = true;
        //   state.error = null;
        // })
        // .addCase(fetchSaleOrdersByIdThunk.fulfilled, (state, action) => {
        //   state.loading = false;
        //   state.tickets = action.payload;
        //   // state.tickets = state.tickets.filter(ticket => ticket.statusSelected === 1);
        // })
        // .addCase(fetchSaleOrdersByIdThunk.rejected, (state, action) => {
        //   state.loading = false;
        //   state.error = action.payload || 'Failed to fetch sale order';
        // })
        .addCase(createTicketThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(createTicketThunk.fulfilled, (state, action) => {
          if(action.payload) {
            state.loading = false;
            state.tickets.push(action.payload);
          }
        })
        .addCase(createTicketThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to save ticket';
        })
        .addCase(updateTicketThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(updateTicketThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.tickets.push(action.payload);
        })
        .addCase(updateTicketThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to save ticket';
        })
        .addCase(deleteTicketThunk.pending, state => {
          state.loading = true;
          state.error = null;
        })
        .addCase(deleteTicketThunk.fulfilled, (state, action) => {
          state.loading = false;
          state.tickets = state.tickets.filter(ticket => ticket.id !== action.payload);
        })
        .addCase(deleteTicketThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to delete customer';
        })
        .addCase(confirmSaleOrderThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(confirmSaleOrderThunk.fulfilled, (state, action) => {
          state.loading = false;
          const confirmedSaleOrder = action.payload;
          const ticketIndex = state.tickets.findIndex(ticket => ticket.saleOrderId === confirmedSaleOrder.saleOrderId);
          if (ticketIndex !== -1) {
            state.tickets[ticketIndex] = confirmedSaleOrder;
          }
        })
        .addCase(confirmSaleOrderThunk.rejected, (state, action) => {
          console.log("payload 2: ", action.payload)
          state.loading = false;
          state.error = action.payload || 'Failed to confirm sale order';
        })
        .addCase(completeSaleOrderThunk.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(completeSaleOrderThunk.fulfilled, (state, action) => {
          state.loading = false;
          const completedSaleOrder = action.payload;
          const ticketIndex = state.tickets.findIndex(ticket => ticket.saleOrderId === completedSaleOrder.saleOrderId);
          if (ticketIndex !== -1) {
            state.tickets[ticketIndex] = completedSaleOrder;
          }
        })
        .addCase(completeSaleOrderThunk.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload || 'Failed to complete sale order';
        });
  },
});

export const selectTicketByNumber = createSelector(
  state => state.tickets.tickets,
  (_, saleOrderSeq) => saleOrderSeq,
  (tickets, saleOrderSeq) => tickets.find(ticket => ticket.saleOrderSeq === saleOrderSeq),
);
export const selectTicketById = createSelector(
    state => state.tickets.tickets,
    (_, id) => id,
    (tickets, id) => tickets.find(ticket => ticket.id === id),
);

export const { saveTicket, updateTicketStatus, removeTicket, refundItems } =
  ticketSlice.actions;
export default ticketSlice.reducer;
