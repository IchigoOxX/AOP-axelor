import React, {useEffect} from "react";
import styled from "styled-components";
import CurrentShift from "./CurrentShift";
import ShiftsList from "./ShiftsList";
import {useDispatch} from "react-redux";
import {fetchShiftsThunk} from "../Redux/shiftSlice";

const Container = styled.div`
  margin: 0 30px;
`;
const H2 = styled.h2`
  font-size: 27px;
  color: #0c085c;
`;

const Main = () => {

    const dispatch = useDispatch();
    useEffect(() => {
        dispatch(fetchShiftsThunk());
    }, [dispatch]);
  
  return (
    <Container>
        <H2>Current Shift</H2>
        <CurrentShift />
        <H2>All Shifts</H2>
        <ShiftsList />
    </Container>
  );
};

export default Main;
