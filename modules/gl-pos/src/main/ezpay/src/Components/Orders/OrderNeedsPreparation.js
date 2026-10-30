import {Alert, CircularProgress, Fade} from "@mui/material";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import styled from "styled-components";
import {clearCart, updateCurrentTicket} from "../Redux/cartSlice";
import {
  completeSaleOrderThunk,
  confirmSaleOrderThunk,
  createTicketThunk,
  saveTicket,
  selectTicketById
} from "../Redux/ticketSlice";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";
import {useNavigate} from "react-router-dom";
import {setTotal} from "../Redux/paymentSlice";

const Popup = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.15);
  color: black;
  z-index: 1;
`;
const Container = styled.div`
  position: relative;
  background-color: #f1f7fb;
  padding: 0 50px 15px 50px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
`;

const Button = styled.button`
  margin: 5px;
  padding: 10px 30px;
  background-color: #0c085c;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
  &:hover {
    background-color: #0a074a;
  }
`;

const ButtonContainer = styled.div`
  display: flex;
  justify-content: center;
  gap: 10px;
`;
const Icon = styled.div`
  position: absolute;
  top: 0;
  right: 0;
  cursor: pointer;
  transition: color 0.3s ease;
  padding: 5px;
  &:hover {
    color: rgba(0, 0, 0, 0.75);
  }
`;
const LoadingContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
`;
const OrderNeedsPreparation = ({
  setShowConfirm,
  confirmNavigation,
  setSaved,
}) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const cartItems = useSelector((state) => state.cart.cartItems);
  const orderNumber = useSelector((state) => state.cart.orderNumber);
  const customer = useSelector((state) => state.customers.selectedCustomer);
  const total = useSelector((state) => state.cart.total);
  const itemsCount = useSelector((state) => state.cart.itemsCount);
  const loading = useSelector((state) => state.tickets.loading);

  const handleYes = async () => {
    const ticket = await handleSaveTicketAsConfirmed();
    if (ticket) {
      // dispatch(updateOrderNumber(orderNumber++));
      if (confirmNavigation) {
        confirmNavigation();
      }
    }
  };
  const handleNo = async () => {

    const ticket = await handleSaveTicketAsConfirmed();
    // const ticket = await fetchSaleOrderById(ticketId);
    if (ticket) {
      setShowConfirm(false);
      navigate('/payment');
      dispatch(setTotal(total));
      dispatch(updateCurrentTicket(ticket))
      // dispatch(clearCart());
      // dispatch(setTotal((parseFloat(ticket.amountInvoiced)).toFixed(3)));
    }
  };

  const handleClose = () => {
    setShowConfirm(false);
  };
  const handleSaveTicketAsConfirmed = async () => {
    let clientPartner = null;
    if (customer) {
      const { id, fullName } = customer;
      clientPartner = { id, fullName };
    }
    const saleOrderLineList = cartItems;

    try {
      // Dispatching createTicketThunk and waiting for it to resolve
      const ticketAction = await dispatch(
          createTicketThunk({
            orderNumber,
            amountInvoiced: total,
            itemsCount,
            clientPartner,
            saleOrderLineList,
            statusSelect: 1,
          })
      );

      // Checking if createTicketThunk was successful
      if (ticketAction.type.endsWith('/fulfilled') && ticketAction.payload) {
        const ticket = ticketAction.payload;

        // Confirm the sale order using the ticket ID
        const confirmAction = await dispatch(confirmSaleOrderThunk(ticket.id));

        if (confirmAction.type.endsWith('/fulfilled')) {
          // Sale order confirmed successfully
          setShowConfirm(false);
          dispatch(clearCart());

          if (setSaved) {
            setSaved(true);
            setTimeout(() => setSaved(false), 1500);
          }

          // Returning the ticket ID
          return ticket;
        } else {
          // Handle the case where confirmSaleOrderThunk failed
          console.error("Failed to confirm sale order:", confirmAction.error);
          return null;
        }
      } else {
        // Handle the case where createTicketThunk failed
        console.error("Failed to create ticket:", ticketAction.error);
        return null;
      }
    } catch (error) {
      console.error("Failed to save and confirm ticket:", error);
      return null;
    }
  };


  return (
    <Popup>
      <Container>
        <Icon onClick={handleClose}>
          <CloseOutlinedIcon />
        </Icon>
        <p>Does this ticket needs preparation?</p>
        {loading ? (
            <LoadingContainer>
              <CircularProgress />
            </LoadingContainer>
        ) : (
            <ButtonContainer>
              <Button onClick={handleYes}>Yes</Button>
              <Button onClick={handleNo}>No</Button>
            </ButtonContainer>)}
      </Container>
    </Popup>
  );
};

export default OrderNeedsPreparation;
