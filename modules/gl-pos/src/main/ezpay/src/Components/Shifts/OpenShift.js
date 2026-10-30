import React, { useState } from "react";
import styled from "styled-components";
import { useDispatch, useSelector } from "react-redux";
import {openShift, openShiftAsync} from "../Redux/shiftSlice"; // Replace with actual path to your shifts slice
import {Alert, CircularProgress, Fade, TextField} from "@mui/material";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  /* background-color: rgba(168, 208, 231, 0.15); */
  background-color: #f1f7fb;
  margin: 0 40px;
  padding: 25px 50px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
`;

const Button = styled.button`
  margin: 5px;
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

const Input = styled(TextField)`
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: white;
`;
const LoadingContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
`;

const OpenShift = () => {
  const dispatch = useDispatch();
  const currentUser = useSelector((state) => state.users.currentUser);
  const loading = useSelector((state) => state.shifts.loading);
  const [showOpenShiftInput, setShowOpenShiftInput] = useState(false);
  const [startingCash, setStartingCash] = useState(0);

  const handleOpenShiftClick = () => {
    setShowOpenShiftInput(true);
  };

  const handleCancelOpenShift = () => {
    setShowOpenShiftInput(false);
    setStartingCash(0);
  };

  const handleConfirmOpenShift = () => {
    if (currentUser) {
      dispatch(openShiftAsync({ startingCash: startingCash, shiftOwner: currentUser }));
      setShowOpenShiftInput(false);}
  };

  return (
      <Container>
        {loading ? (
            <LoadingContainer>
              <CircularProgress />
            </LoadingContainer>
        ) : showOpenShiftInput ? (
          <>
            <Input
              label="Starting Cash"
              variant="outlined"
              size="small"
              type="number"
              value={startingCash}
              onChange={(e) => setStartingCash(e.target.value)}
            />
            <div>
              <Button onClick={handleConfirmOpenShift}>Confirm</Button>
              <Button onClick={handleCancelOpenShift}>Cancel</Button>
            </div>
          </>
        ) : (
          <Button onClick={handleOpenShiftClick}>Open Shift</Button>
        )}
      </Container>
  );
};

export default OpenShift;
