import React, { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';
import PersonIcon from '@mui/icons-material/Person';
import Popup from '../Common/Popup';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCustomersThunk, selectCustomer } from '../Redux/customerSlice';

const SearchContainer = styled.div`
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
`;
const SearchInput = styled.input`
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
`;
const CustomerListContainer = styled.div`
  max-height: 150px;
  overflow-y: auto;
`;
const Customer = styled.div`
  display: flex;
  align-items: center;
  padding: 5px;
  background-color: ${props => (props.selected ? '#a8d0e740' : 'transparent')};
  &:hover {
    background-color: ${props => (props.selected ? '#a8d0e740' : '#f5f5f5')};
  }
`;

const CustomerName = styled.div`
  margin-left: 10px;
  font-size: 16px;
  cursor: pointer;
`;
const AddCustomerButton = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #007bff;
  margin-bottom: 5px;
  &:hover {
    color: #0056b3;
  }
`;

const CustomersList = props => {
  const dispatch = useDispatch();
  const customers = useSelector(state => state.customers.customersList);
  const selectedCustomer = useSelector(state => state.customers.selectedCustomer);
  const loading = useSelector(state => state.customers.loading);
  const error = useSelector(state => state.customers.error);
  const [filteredCustomers, setFilteredCustomers] = useState([]);
  const listContainerRef = useRef(null);
  useEffect(() => {
    dispatch(fetchCustomersThunk())
      .then(response => {
        console.log('Customers fetched:', response.payload);
      })
      .catch(error => {
        console.error('Error fetching customers:', error);
      });
  }, [dispatch]);
  useEffect(() => {
    setFilteredCustomers(customers);
  }, [customers]);
  useEffect(() => {
    // Scroll to selected customer when the component mounts or selectedCustomer changes
    if (listContainerRef.current && selectedCustomer) {
      const selectedCustomerElement = listContainerRef.current.querySelector(`.customer-item-${selectedCustomer.id}`);
      if (selectedCustomerElement) {
        selectedCustomerElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
        });
      }
    }
  }, [selectedCustomer, customers]);
  const handleSearch = event => {
    const searchQuery = event.target.value.toLowerCase();
    const filtered = customers.filter(customer => customer.simpleFullName.toLowerCase().includes(searchQuery));
    setFilteredCustomers(filtered);
  };

  const handleCustomerClick = (customer, event) => {
    event.stopPropagation();
    if (selectedCustomer && selectedCustomer.id === customer.id) {
      dispatch(selectCustomer(null));
    } else {
      dispatch(selectCustomer(customer));
    }
    props.closeCustomersList(event);
  };

  const handleAddNewCustomer = event => {
    props.openCustomerForm(event);
    props.closeCustomersList(event);
  };
  return (
    <Popup title="Customers List" onClose={props.closeCustomersList}>
      {loading && <div>Loading...</div>}
      {error && <div>Error: {error}</div>}
      {!loading && !error && (
        <>
          <SearchContainer>
            <SearchInput type="text" placeholder="Search customers..." onChange={handleSearch} />
          </SearchContainer>
          <AddCustomerButton onClick={handleAddNewCustomer}>
            <AddOutlinedIcon />
            Add New Customer
          </AddCustomerButton>
          <CustomerListContainer ref={listContainerRef}>
            {filteredCustomers.map(customer => (
              <Customer
                key={customer.id}
                onClick={event => handleCustomerClick(customer, event)}
                selected={selectedCustomer && selectedCustomer.id === customer.id}
                className={`customer-item-${customer.id}`}
              >
                <PersonIcon />
                <CustomerName>{customer.simpleFullName}</CustomerName>
              </Customer>
            ))}
          </CustomerListContainer>
        </>
      )}
    </Popup>
  );
};

export default CustomersList;
