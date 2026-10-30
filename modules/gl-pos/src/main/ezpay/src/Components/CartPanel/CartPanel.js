import React, {useEffect, useState} from 'react';
import styled from 'styled-components';
import PurchasedItem from './PurchasedItem';
import Total from './Total';
import CustomersList from './CustomersList';
import { Calculator } from '../Common/Calculator';
import CustomerForm from './CustomerForm';
import {useDispatch, useSelector} from 'react-redux';
import PersonIcon from '@mui/icons-material/Person';
import ReceiptLongOutlinedIcon from '@mui/icons-material/ReceiptLongOutlined';
import OpenTickets from './OpenTickets';
import TicketDetails from './TicketDetails';
import {setOrderNumber} from "../Redux/cartSlice";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-left: solid 1px #ccc;
  padding: 10px;
  font-family: Arial, sans-serif;
  min-width: 300px;
  height: calc(100vh - 110px);
`;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
`;
const Button = styled.button`
  display: flex;
  align-items: center;
  gap: 2px;
  border: none;
  background-color: white;
  color: #0c085c;
  font-weight: bold;
  font-size: 18px;
  cursor: pointer;
`;
const OpenTicketButtonContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
`;
const OpenTicketButton = styled.button`
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border: 1px solid #ccc;
  border-radius: 20px;
  box-shadow: 0 2px 10px 1px rgba(0, 0, 0, 0.1);
  background-color: white;
  color: #0c085c;
  font-weight: bold;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  &:hover {
    background-color: #0c085c;
    color: white;
  }
`;
const H2 = styled.h2`
  margin: 0;
  color: #0c085c;
  font-weight: bold;
  font-size: 20px;
`;
const P = styled.p`
  margin: 4px 0 0 2px;
  color: #868686;
  font-size: 12px;
`;
const PurchasedItemsContainer = styled.div`
  margin: 10px 0;
  padding-right: 2px;
  height: 100%;
  overflow-y: auto;
`;
const CartPanel = () => {
  const dispatch = useDispatch();
  const orderNumber = useSelector(state => state.cart.orderNumber);
  const cartItems = useSelector(state => state.cart.cartItems);
  const selectedCustomer = useSelector(state => state.customers.selectedCustomer);
  const tickets = useSelector(state => state.tickets.tickets)
  const openTickets = useSelector(state => state.tickets.tickets.filter(ticket => ticket.statusSelect === 1));

  const [showCustomersList, setShowCustomersList] = useState(false);
  const [showCustomerForm, setShowCustomerForm] = useState(false);
  const [showOpenedTickets, setShowOpenTickets] = useState(false);
  const [showTicketDetails, setShowTicketDetails] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState(null);

  console.log('selected customer: ', selectedCustomer)
  useEffect(() => {
    if (tickets) {
      console.log('tickets: ', tickets)
      console.log("last order: ",tickets[0]?.orderNumber);
      dispatch(setOrderNumber(tickets[0]?.orderNumber));
    }
  }, [tickets]);

  const openComponent = (event, setState) => {
    event.stopPropagation();
    setState(true);
  };
  const closeComponent = (event, setState) => {
    event.stopPropagation();
    setState(false);
  };
  const openTicketDetails = (event, ticket) => {
    setSelectedTicket(ticket);
    openComponent(event, setShowTicketDetails);
  };

  return (
    <Container>
      <Header>
        <div>
          <H2>Order Menu</H2>
          <P>Order N°: {orderNumber || '...'}</P>
        </div>
        <Button onClick={event => openComponent(event, setShowCustomersList)}>
          <PersonIcon sx={{ fontSize: 30 }} />
          {selectedCustomer && !(selectedCustomer.fullName?.includes("Unknown")) ? selectedCustomer.simpleFullName : 'Select Customer'}
        </Button>
        {showCustomersList && (
          <CustomersList
            closeCustomersList={event => closeComponent(event, setShowCustomersList)}
            openCustomerForm={event => openComponent(event, setShowCustomerForm)}
          />
        )}
        {showCustomerForm && (
          <CustomerForm
            closeCustomerForm={event => closeComponent(event, setShowCustomerForm)}
            closeCustomersList={event => closeComponent(event, setShowCustomersList)}
            openCustomersList={event => openComponent(event, setShowCustomersList)}
          />
        )}
      </Header>
      {cartItems.length === 0 ? (
        <>
          {openTickets.length !== 0 && (
            <OpenTicketButtonContainer>
              <OpenTicketButton onClick={event => openComponent(event, setShowOpenTickets)}>
                <ReceiptLongOutlinedIcon sx={{ fontSize: 25 }} />
                {openTickets.length} Open Ticket
                {openTickets.length > 1 ? 's' : ''}
              </OpenTicketButton>
            </OpenTicketButtonContainer>
          )}
          {showOpenedTickets && (
            <OpenTickets
              closeOpenTickets={event => closeComponent(event, setShowOpenTickets)}
              openTicketDetails={openTicketDetails}
            />
          )}
          {showTicketDetails && selectedTicket && (
            <TicketDetails
                saleOrderSeq={selectedTicket.saleOrderSeq}
              closeTicketDetails={event => closeComponent(event, setShowTicketDetails)}
              openTicketsList={event => openComponent(event, setShowOpenTickets)}
            />
          )}
        </>
      ) : (
        <PurchasedItemsContainer>
          {cartItems.map((product, index) => (
            <PurchasedItem key={index} product={product} />
          ))}
        </PurchasedItemsContainer>
      )}

      <div>
        <Calculator additionalButtons={true} />
        <Total />
      </div>
    </Container>
  );
};

export default CartPanel;
