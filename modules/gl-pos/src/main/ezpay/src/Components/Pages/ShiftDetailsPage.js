import React from 'react';
import styled from 'styled-components';
import Page from '../Common/Page';
import ArrowBackOutlinedIcon from '@mui/icons-material/ArrowBackOutlined';
import { useLocation, useNavigate } from 'react-router-dom';
import ShiftDetails from "../Shifts/ShiftDetails";

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

const ShiftDetailsPage = () => {
  const navigate = useNavigate();
  const { state } = useLocation();
  const { shift } = state || { shift: null };

  if (!shift) {
    return <div>No shift data available.</div>;
  }

  const handleGoBack = () => {
    navigate("/shifts");
  };

  return (
    <Page>
      <Container>
        <Header>
          <Title>
            <ArrowBackOutlinedIcon fontSize="large" style={{ cursor: 'pointer' }} onClick={handleGoBack} />
            <h2>Shift Details</h2>
          </Title>
        </Header>
        <ShiftDetails shift={shift} />
      </Container>
    </Page>
  );
};

export default ShiftDetailsPage;
