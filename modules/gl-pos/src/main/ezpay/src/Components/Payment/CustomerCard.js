import React from 'react';
import { useSelector } from 'react-redux';
import styled from 'styled-components';
import AccountBoxOutlinedIcon from '@mui/icons-material/AccountBoxOutlined';

const Container = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
  border: solid black 1px;
  padding: 5px 10px;
`;
const Name = styled.div`
  font-size: 22px;
  font-weight: 500;
  margin-bottom: 5px;
`;
const Details = styled.div`
  font-size: 14px;
  color: gray;
`;
const CustomerCard = () => {
  const customer = useSelector(state => state.customers.selectedCustomer);
  return (
    <Container>
      <AccountBoxOutlinedIcon fontSize="large" />
      <div>
        <Name>{customer.simpleFullName}</Name>
        <Details>{customer?.mobilePhone? customer.mobilePhone : customer?.fixedPhone? customer.fixedPhone : "No Number"}</Details>
        <Details>{customer.mainAddress ? customer.mainAddress.fullName : "No Adrdress"}</Details>
      </div>
    </Container>
  );
};

export default CustomerCard;
