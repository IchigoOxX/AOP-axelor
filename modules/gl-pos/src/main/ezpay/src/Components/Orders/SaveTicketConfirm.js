import { Alert, Fade } from "@mui/material";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import styled from "styled-components";
import {clearCart, setOrderNumber} from "../Redux/cartSlice";
import {createTicketThunk, saveTicket} from "../Redux/ticketSlice";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";

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
const SaveTicketConfirm = ({
  setShowSave,
  confirmNavigation,
  setSaved,
}) => {
  const dispatch = useDispatch();
  const cartItems = useSelector((state) => state.cart.cartItems);
  let orderNumber = useSelector((state) => state.cart.orderNumber);
  const customer = useSelector((state) => state.customers.selectedCustomer);
  const total = useSelector((state) => state.cart.total);
  const itemsCount = useSelector((state) => state.cart.itemsCount);

  const tickets = useSelector((state) => state.tickets.tickets);
  const handleConfirm = () => {
    handleSaveTicket();
    dispatch(setOrderNumber(orderNumber))
    if (confirmNavigation) {
      confirmNavigation();
    }
  };
  const handleClose = () => {
    setShowSave(false);
  };
  const handleCancel = () => {
    setShowSave(false);
    dispatch(clearCart());
    if (confirmNavigation) {
      confirmNavigation();
    }
  };
  const handleSaveTicket = () => {
    let clientPartner = null;
    if (customer) {
      const {id, fullName } = customer;
      clientPartner = { id, fullName };
    }
    const saleOrderLineList = cartItems;
    dispatch(
        createTicketThunk({
          orderNumber,
          amountInvoiced: total,
          itemsCount,
          clientPartner,
          saleOrderLineList,
          statusSelect: 1
        })
    );
    setShowSave(false);
    dispatch(clearCart());
    if (!setSaved) {
      setSaved(true);
      setTimeout(() => setSaved(false), 1500);
    }
  };

  return (
    <Popup>
      <Container>
        <Icon onClick={handleClose}>
          <CloseOutlinedIcon />
        </Icon>
        <p>Do you want to save this ticket?</p>
        <ButtonContainer>
          <Button onClick={handleConfirm}>Yes</Button>
          <Button onClick={handleCancel}>No</Button>
        </ButtonContainer>
      </Container>
    </Popup>
  );
};

export default SaveTicketConfirm;
