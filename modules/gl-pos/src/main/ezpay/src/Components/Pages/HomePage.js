import React, {useEffect} from "react";
import Main from "../Products/Main";
import Page from "../Common/Page";
import {fetchShiftsThunk} from "../Redux/shiftSlice";
import {useDispatch} from "react-redux";
import {fetchSaleOrdersThunk} from "../Redux/ticketSlice";
import {setOrderNumber} from "../Redux/cartSlice";
import {fetchPaymentModesThunk} from "../Redux/paymentSlice";

const HomePage = () => {
    const dispatch = useDispatch();
    useEffect(() => {
        dispatch(fetchShiftsThunk());
        dispatch(fetchSaleOrdersThunk());
        dispatch(fetchPaymentModesThunk())
    }, [dispatch]);
  return (
    <Page>
      <Main />
    </Page>
  );
};

export default HomePage;
