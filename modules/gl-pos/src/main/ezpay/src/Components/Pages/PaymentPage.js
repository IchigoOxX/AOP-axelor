import React, { useState } from 'react';
import styled from 'styled-components';
import Paymentmodes from '../Payment/PaymentModes';
import KeyboardDoubleArrowLeftOutlinedIcon from '@mui/icons-material/KeyboardDoubleArrowLeftOutlined';
import { useNavigate } from 'react-router-dom';
import MiddlePanel from '../Payment/MiddlePanel';
import RightPanel from '../Payment/RightPanel';
import TicketContinue from '../Orders/TicketContinue';

const PageContainer = styled.div`
  height: calc(100vh - 10px);
  display: flex;
  flex-direction: column;
`;
const Container = styled.div`
  height: 100%;
  margin: 10px 0;
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  overflow: hidden;
`;
const Header = styled.div`
  display: flex;
  align-items: center;
  margin-top: 10px;
`;
const BackButton = styled.button`
  position: absolute;
  left: 20px;
  padding: 3px 6px;
  display: flex;
  align-items: center;
  column-gap: 3px;
  max-height: 35px;
  font-size: 18px;
  text-decoration: none;
  color: #565656;
  background-color: white;
  border: solid #bbbbbb 1px;
  border-radius: 5px;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.25);
`;
const H1 = styled.h1`
  margin: auto;
`;

const PaymentPage = () => {
  const navigate = useNavigate();
  const [showContinue, setShowContinue] = useState(false);

  const handleBack = () => {
    // dispatch(resetPayments());
    setShowContinue(true);
  };

  const confirmNavigation = () => {
    setShowContinue(false);
    navigate('/');
    console.log('in 2');
  };

  return (
    <PageContainer>
      <Header>
        <BackButton onClick={handleBack}>
          <KeyboardDoubleArrowLeftOutlinedIcon />
          Back
        </BackButton>
        {showContinue && <TicketContinue setShowContinue={setShowContinue} confirmNavigation={confirmNavigation} />}
        <H1>PAYMENT</H1>
      </Header>
      <Container>
        <Paymentmodes />
        <MiddlePanel />
        <RightPanel />
      </Container>
    </PageContainer>
  );
};

export default PaymentPage;
