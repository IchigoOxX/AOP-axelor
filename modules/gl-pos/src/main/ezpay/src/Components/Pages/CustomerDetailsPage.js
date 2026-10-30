import React, {useState} from 'react';
import styled from 'styled-components';
import Page from '../Common/Page';
import CustomerDetails from '../Customers/CustomerDetails';
import ButtonComponent from '../Common/ButtonComponent';
import ModeEditOutlineOutlinedIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import ArrowBackOutlinedIcon from '@mui/icons-material/ArrowBackOutlined';
import { useLocation, useNavigate } from 'react-router-dom';
import {deleteCustomerThunk} from "../Redux/customerSlice";
import {useDispatch} from "react-redux";
import DeleteConfirm from "../Common/DeleteConfirm";

const Container = styled.div`
  padding: 0 30px 10px 30px;
`;
const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;
const Title = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 18px;
  color: #0c085c;
`;
const Buttons = styled.div`
  display: flex;
  gap: 7px;
`;

const CustomerDetailsPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { customer = {}, mode = null } = location.state;

  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false); // State for showing the delete confirmation

  const handleEdit = customer => {
    navigate('/customers/customer-details', {
      state: { customer, mode: 'edit' },
    });
  };

  const handleDelete = () => {
    setShowDeleteConfirm(true); // Show the delete confirmation dialog
  };

  const handleConfirmDelete = () => {
    dispatch(deleteCustomerThunk(customer.id)).then(() => {
      navigate("/customers");
    });
  };

  const handleCancelDelete = () => {
    setShowDeleteConfirm(false); // Hide the delete confirmation dialog
  };

  const handleGoBack = () => {
    navigate("/customers");
  };

  return (
    <Page>
      <Container>
        <Header>
          <Title>
            <ArrowBackOutlinedIcon fontSize="large" style={{ cursor: 'pointer' }} onClick={handleGoBack} />
            <h2>Customer Details</h2>
          </Title>
          <Buttons>
            {mode !== 'add' && (
              <>
                {mode !== 'edit' && (
                  <ButtonComponent
                    title="Edit"
                    icon={<ModeEditOutlineOutlinedIcon fontSize="sm" />}
                    handleClick={() => handleEdit(customer)}
                  />
                )}
                <ButtonComponent
                  title="Delete"
                  icon={<DeleteOutlineOutlinedIcon fontSize="sm" />}
                  handleClick={() => handleDelete(customer.id)}
                />
              </>
            )}
          </Buttons>
        </Header>
        <CustomerDetails customer={customer} mode={mode} />
        {showDeleteConfirm && (
            <DeleteConfirm onConfirm={handleConfirmDelete} onClose={handleCancelDelete} />
        )}
      </Container>
    </Page>
  );
};

export default CustomerDetailsPage;
