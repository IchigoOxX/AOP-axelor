import React from "react";
import styled from "styled-components";
import { useSelector } from "react-redux";
import { useLocation, useNavigate } from "react-router-dom";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";

const Container = styled.div`
  margin: 20px auto;
  padding: 20px;
  max-width: 800px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
`;

const Header = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 20px;
`;

const BackButton = styled(ArrowBackIcon)`
  cursor: pointer;
  color: #0c085c;
  margin-right: 10px;
  &:hover {
    color: #0a074a;
  }
`;

const Title = styled.h1`
  font-size: 24px;
  color: #0c085c;
  margin: 0;
`;

const DetailsSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
`;

const Detail = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
`;

const Label = styled.div`
  font-size: 16px;
  color: #555;
`;

const Value = styled.div`
  font-size: 16px;
  font-weight: bold;
  color: #0c085c;
`;

const ShiftDetails = ({shift}) => {

  const getStatusLabel = (status) => {
    switch (status) {
      case 0:
        return 'Open';
      case 1:
        return 'Closed';
    }
  };

  return (
    <Container>
      <DetailsSection>
        <Detail>
          <Label>Shift Number:</Label>
          <Value>{shift.shiftNumber}</Value>
        </Detail>
        <Detail>
          <Label>Shift Owner:</Label>
          <Value>{shift.cashier?.fullName}</Value>
        </Detail>
        <Detail>
          <Label>Shift Date:</Label>
          <Value>{new Date(shift.shiftDate).toLocaleDateString()}</Value>
        </Detail>
        <Detail>
          <Label>Opening Hour:</Label>
          <Value>{new Date(shift.openingHour).toLocaleTimeString()}</Value>
        </Detail>
        <Detail>
          <Label>Closing Hour:</Label>
          <Value>{shift.closingHour ? new Date(shift.closingHour).toLocaleTimeString() : '_'}</Value>
        </Detail>
        <Detail>
          <Label>Starting Cash:</Label>
          <Value>${parseFloat(shift.startingCash).toFixed(3)}</Value>
        </Detail>
        <Detail>
          <Label>Sales Amount:</Label>
          <Value>${parseFloat(shift.totalSales).toFixed(3)}</Value>
        </Detail>
        <Detail>
          <Label>Refunds Amount:</Label>
          <Value>${parseFloat(shift.totalRefunds).toFixed(3)}</Value>
        </Detail>
        <Detail>
          <Label>Discounts Amount:</Label>
          <Value>${parseFloat(shift.totalDiscounts).toFixed(3)}</Value>
        </Detail>
        <Detail>
          <Label>Total Expected Cash:</Label>
          <Value>${parseFloat(shift.cashBalance).toFixed(3)}</Value>
        </Detail>
        <Detail>
          <Label>Status:</Label>
          <Value>{getStatusLabel(shift.status)}</Value>
        </Detail>
      </DetailsSection>
    </Container>
  );
};

export default ShiftDetails;
