import React, { useState } from 'react';
import styled from 'styled-components';
import CustomersList from '../Customers/CustomersList';
import FilterComponent from '../Customers/FilterComponent';
import PersonAddAltOutlinedIcon from '@mui/icons-material/PersonAddAltOutlined';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import { useNavigate } from 'react-router-dom';
import ButtonComponent from '../Common/ButtonComponent';
import { useDispatch } from 'react-redux';
import { deleteCustomerThunk } from '../Redux/customerSlice';
import DeleteConfirm from "../Common/DeleteConfirm";

const Container = styled.div`
  margin: 0 30px;
`;
const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;
const H2 = styled.h2`
  font-size: 27px;
  color: #0c085c;
`;
const Buttons = styled.div`
  display: flex;
  gap: 7px;
`;

const Main = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [filters, setFilters] = useState({
    mostPurchases: false,
    activeCustomers: false,
    inactiveCustomers: false,
  });
  const [showButton, setShowButton] = useState(false);
  const [selectedCustomers, setSelectedCustomers] = useState([]);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false); // State for showing the delete confirmation

  const handleClick = () => {
    navigate('/customers/customer-details', {
      state: { mode: 'add' },
    });
  };

  const handleDeleteMass = () => {
    setShowDeleteConfirm(true); // Show the delete confirmation dialog
  };

  const handleConfirmDelete = () => {
    selectedCustomers.forEach(customerId => {
      dispatch(deleteCustomerThunk(customerId));
    });
    setSelectedCustomers([]);
    setShowButton(false);
    setShowDeleteConfirm(false); // Hide the delete confirmation dialog
  };

  const handleCancelDelete = () => {
    setShowDeleteConfirm(false); // Hide the delete confirmation dialog
  };
  // const handleClick = () => {
  //   navigate('/customers/customer-details', {
  //     state: { mode: 'add' },
  //   });
  // };
  // const handleDeleteMass = () => {
  //   selectedCustomers.forEach(customerId => {
  //     dispatch(deleteCustomerThunk(customerId));
  //   });
  //   setSelectedCustomers([]);
  //   setShowButton(false);
  // };

  return (
    <Container>
      <Header>
        <H2>Customers List</H2>
        <Buttons>
          {showButton && (
            <ButtonComponent
              title="Delete"
              icon={<DeleteOutlineOutlinedIcon fontSize="sm" />}
              handleClick={handleDeleteMass}
            />
          )}
          <ButtonComponent
            title="New Customer"
            icon={<PersonAddAltOutlinedIcon fontSize="sm" />}
            handleClick={handleClick}
          />
          <FilterComponent filters={filters} setFilters={setFilters} />
        </Buttons>
      </Header>
      <CustomersList
          filters={filters}
        setShowButton={setShowButton}
        selectedCustomers={selectedCustomers}
        setSelectedCustomers={setSelectedCustomers}
      />
      {showDeleteConfirm && (
          <DeleteConfirm onConfirm={handleConfirmDelete} onClose={handleCancelDelete} />
      )}
    </Container>
  );
};

export default Main;
