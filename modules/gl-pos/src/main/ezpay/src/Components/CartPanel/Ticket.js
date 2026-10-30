import React, { useState } from 'react';
import styled from 'styled-components';
import MoreVertOutlinedIcon from '@mui/icons-material/MoreVertOutlined';
import { Menu, MenuItem } from '@mui/material';
import TicketDetails from './TicketDetails';
import { useDispatch } from 'react-redux';
import {updateCartItems, updateCurrentTicket} from '../Redux/cartSlice';
import {deleteTicketThunk, removeTicket} from '../Redux/ticketSlice';
import { selectCustomer } from '../Redux/customerSlice';

const Container = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  border: solid black 1px;
  border-radius: 10px;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.25);
  padding: 2px 10px;
`;
const TicketInfo = styled.div`
  display: flex;
  gap: 20px;
`;
const Div = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  & > * {
    padding: 2px;
  }
`;

const Ticket = ({ ticket, openTicketDetails, onClose }) => {
  const dispatch = useDispatch();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleClick = event => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleDetails = event => {
    console.log("ticket details: ", ticket)
    openTicketDetails(event, ticket);
    setAnchorEl(null);
  };
  const handleContinue = event => {
    console.log('continue ticket: ', ticket)
    dispatch(updateCurrentTicket(ticket))
    dispatch(updateCartItems(ticket.saleOrderLineList));
    // dispatch(setOrderNumber());
    dispatch(selectCustomer(ticket.clientPartner));
    handleClose();
    onClose(event);
  };

  const handleDelete = () => {
    dispatch(deleteTicketThunk(ticket.id));
    handleClose();
  };
  return (
    <Container>
      <TicketInfo>
        <Div>
          <div>Ticket N° {ticket.saleOrderSeq}</div>
          <div>{ticket.creationDate}</div>
        </Div>
        <Div>
          <div>Total: {parseFloat(ticket.amountInvoiced).toFixed(3)}</div>
          <div>Customer: {ticket.clientPartner ? ticket.clientPartner.simpleFullName : 'No Customer'}</div>
        </Div>
      </TicketInfo>
      <div>
        <MoreVertOutlinedIcon
          aria-controls="simple-menu"
          aria-haspopup="true"
          onClick={handleClick}
          style={{ cursor: 'pointer' }}
        />
        <Menu id="simple-menu" anchorEl={anchorEl} keepMounted open={Boolean(anchorEl)} onClose={handleClose}>
          <MenuItem onClick={handleDetails}>Details</MenuItem>
          <MenuItem onClick={handleContinue}>Continue</MenuItem>
          <MenuItem onClick={handleDelete}>Delete</MenuItem>
        </Menu>
      </div>
    </Container>
  );
};

export default Ticket;
