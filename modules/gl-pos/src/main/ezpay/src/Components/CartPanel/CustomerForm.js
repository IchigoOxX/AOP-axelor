import React, { useState } from 'react';
import Popup from '../Common/Popup';
import styled from 'styled-components';
import { useDispatch } from 'react-redux';
import { addCustomerThunk, selectCustomer } from '../Redux/customerSlice';
import CustomerDetails from '../Customers/CustomerDetails';

const Container = styled.div`
  height: calc(100vh - 120px);
  overflow-y: auto;
`;
const CustomerForm = props => {
  const returnToHomePage = event => {
    props.closeCustomerForm(event);
    props.closeCustomersList(event);
  };
  const returnToCustomerList = event => {
    props.closeCustomerForm(event);
    props.openCustomersList(event);
  };

  return (
    <Popup title="Customer Form" onClose={returnToCustomerList}>
      <Container>
        <CustomerDetails customer={{}} mode={'add'} returnToHomePage={returnToHomePage} />
      </Container>
    </Popup>
  );
};

export default CustomerForm;
