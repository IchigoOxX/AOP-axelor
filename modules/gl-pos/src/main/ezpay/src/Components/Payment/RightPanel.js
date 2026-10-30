import React from 'react';
import styled from 'styled-components';
import Receipt from '../Orders/Receipt';
import CustomerCard from './CustomerCard';
import { useSelector } from 'react-redux';

const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin: 20px auto;
  gap: 10px;
`;

const RightPanel = () => {
  const currentTicket = useSelector(state => state.cart.currentTicket);
  const selectedCustomer = useSelector(state => state.customers.selectedCustomer)
  console.log("current ticket in pay: ", currentTicket)
  const customer = useSelector(state => state.customers.selectedCustomer);
  return (
    <Container>
      {customer && !selectedCustomer.fullName.includes("Unknown") && <CustomerCard />}
      <Receipt ticket={currentTicket} />
    </Container>
  );
};

export default RightPanel;
