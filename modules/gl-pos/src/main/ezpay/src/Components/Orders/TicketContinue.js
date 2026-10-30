import { Alert, Fade } from "@mui/material";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import styled from "styled-components";
import { clearCart, setCartFromTicket } from "../Redux/cartSlice";
import {selectTicketByNumber, updateTicketStatus, updateTicketThunk} from "../Redux/ticketSlice";
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
const TicketContinue = ({ setShowContinue, confirmNavigation }) => {
  const dispatch = useDispatch();
  // const order = useSelector(state => selectTicketByNumber(state, saleOrderSeq));
  // const currentTicket = useSelector((state) =>
  //   state.tickets.tickets.find(ticket => ticket.status === "current")
  // );
  const handleClose = () => {
    setShowContinue(false);
  };
  const handleConfirm = () => {
    setShowContinue(false);
    // dispatch(setCartFromTicket({ticket: currentTicket}))
    if (confirmNavigation) {
      confirmNavigation();
    }
  };
  const handleCancel = () => {
    setShowContinue(false);
    dispatch(clearCart());
    // dispatch(updateTicketStatus({ saleOrderSeq: currentTicket.saleOrderSeq, newStatus: "open" }));
    // dispatch(updateTicketThunk({ saleOrderId: ticket.id, status: 4 }));
    if (confirmNavigation) {
      confirmNavigation();
    }
  };

  return (
    <Popup>
      <Container>
        <Icon onClick={handleClose}>
          <CloseOutlinedIcon />
        </Icon>
        <p>Do you want to continue with this ticket?</p>
        <ButtonContainer>
          <Button onClick={handleConfirm}>Yes</Button>
          <Button onClick={handleCancel}>No</Button>
        </ButtonContainer>
      </Container>
    </Popup>
  );
};

export default TicketContinue;
