import React from "react";
import styled from "styled-components";
import { useSelector, useDispatch } from "react-redux";
import { closeShiftAsync } from "../Redux/shiftSlice"; // Replace with actual path to your shifts slice
import OpenShift from "./OpenShift"; // Import the new OpenShift component

const Content = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  background-color: #f1f7fb;
  margin: 0 40px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
`;

const Details = styled.div`
  display: flex;
  justify-content: space-between;
  font-size: 18px;
  padding: 5px;
`;

const Label = styled.div`
  color: #555;
`;

const Value = styled.div`
  font-weight: bold;
  color: #0c085c;
`;

const Button = styled.button`
  float: right;
  padding: 10px 30px;
  background-color: #0c085c;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
  &:hover {
    background-color: #0a074a;
  }
`;

const CurrentShift = () => {
  const dispatch = useDispatch();
  const currentShift = useSelector((state) => state.shifts.currentShift);
  const handleCloseShift = () => {
    dispatch(closeShiftAsync());
  };

  return (
      <>
        {!currentShift ? (
            <OpenShift />
        ) : (
            <Content>
              <div>
                <Details>
                  <Label>Starting Cash:</Label>
                  <Value>${parseFloat(currentShift.startingCash).toFixed(3)}</Value>
                </Details>
                <Details>
                  <Label>Sales Amount:</Label>
                  <Value>${parseFloat(currentShift.totalSales).toFixed(3)}</Value>
                </Details>
                <Details>
                  <Label>Refunds Amount:</Label>
                  <Value>${parseFloat(currentShift.totalRefunds).toFixed(3)}</Value>
                </Details>
                <Details>
                  <Label>Total Expected Cash:</Label>
                  <Value>${parseFloat(currentShift.cashBalance).toFixed(3)}</Value>
                </Details>
              </div>
              <div>
                <Details>
                  <Label>Shift Number:</Label>
                  <Value>{currentShift.shiftNumber}</Value>
                </Details>
                <Details>
                  <Label>Shift Date:</Label>
                  <Value>{new Date(currentShift.shiftDate).toLocaleDateString()}</Value>
                </Details>
                <Details>
                  <Label>Shift Opened At:</Label>
                  <Value>{new Date(currentShift.openingHour).toLocaleTimeString()}</Value>
                </Details>
                <Details>
                  <Label>Shift Owner:</Label>
                  <Value>{currentShift.cashier.fullName}</Value>
                </Details>
                <Button onClick={handleCloseShift}>Close Shift</Button>
              </div>
            </Content>
        )}
      </>
  );
};

export default CurrentShift;
