import { Alert, Fade } from '@mui/material';
import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {Link, useNavigate} from 'react-router-dom';
import styled from 'styled-components';
import {createTicketThunk, saveTicket, selectTicketById, selectTicketByNumber} from '../Redux/ticketSlice';
import { setTotal } from '../Redux/paymentSlice';
import SaveTicketConfirm from '../Orders/SaveTicketConfirm';
import OrderNeedsPreparation from "../Orders/OrderNeedsPreparation";

const Container = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 5px 10px;
  border-radius: 15px;
  background-color: #a8d0e7;
`;
const TotalCount = styled.div`
  font-size: 22px;
`;
const ItemCount = styled.div`
  color: #444242;
  font-size: 12px;
`;
const Button = styled.button`
  margin-right: 5px;
  padding: 3px 5px;
  background-color: white;
  border: solid grey 1px;
  border-radius: 5px;
  font-size: 15px;
  cursor: pointer;
`;
const AlertNotif = styled(Alert)`
  position: absolute;
  top: -40px;
  right: 45%;
  z-index: 20;
`;

const Total = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const total = useSelector(state => state.cart.total);
  const itemsCount = useSelector(state => state.cart.itemsCount);
  const cartItems = useSelector(state => state.cart.cartItems);
  const currentTicket = useSelector(state => state.cart.currentTicket);
  const customer = useSelector(state => state.customers.selectedCustomer);
  const [showAlert, setShowAlert] = useState(false);
  const [saved, setSaved] = useState(false);
  const [showSave, setShowSave] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);


    const emptyCart = () => {
    if (itemsCount === 0) {
      setShowAlert(true);
      setTimeout(() => setShowAlert(false), 1500);
    }
  };

  const handleSave = () => {
    setShowSave(true);
  };

    const handleConfirm = () => {
        if (currentTicket?.statusSelect === 3) {
            navigate('/payment');
            dispatch(setTotal(total))
        } else {
            setShowConfirm(true);
        }
    };
  // const handlePay = () => {
  //   dispatch(
  //     saveTicket({
  //       saleOrderSeq: orderNumber,
  //       customer: selectedCustomer,
  //       total,
  //       itemsCount,
  //       saleOrderLineList: cartItems,
  //       status: 'current',
  //     }),
  //   );
  //   dispatch(setTotal(total));
  // };

    const handlePay = () => {
        let clientPartner = null;
        if (customer) {
            const {id, fullName } = customer;
            clientPartner = { id, fullName };
        }
        const saleOrderLineList = cartItems;
        dispatch(
            createTicketThunk({
                // saleOrderSeq: orderNumber,
                amountInvoiced: total,
                itemsCount,
                clientPartner,
                saleOrderLineList,
                statusSelect: 1
            })
        );
        dispatch(setTotal(total));
    };

  return (
    <Container>
      <div>
        <TotalCount>Total : {total.toFixed(3)}</TotalCount>
        <ItemCount>Items : {itemsCount} </ItemCount>
      </div>
      <div>
        {itemsCount === 0 ? (
          <>
            <Button onClick={emptyCart}>Save</Button>
            <Button onClick={emptyCart}>Pay</Button>
          </>
        ) : (
          <>
            <Button onClick={handleSave}>Save</Button>
              <Button onClick={handleConfirm}>Pay</Button>
            {/*  <Link to={'/payment'} onClick={handlePay}>*/}
            {/*  <Button>Pay</Button>*/}
            {/*</Link>*/}
          </>
        )}
        {showSave && <SaveTicketConfirm setShowSave={setShowSave} setSaved={setSaved} />}
          {showConfirm && <OrderNeedsPreparation setShowConfirm={setShowConfirm} setSaved={setSaved} />}
        <Fade in={saved}>
          <AlertNotif severity="success">The ticket is saved.</AlertNotif>
        </Fade>
        <Fade in={showAlert}>
            <AlertNotif severity="error">The cart is empty!</AlertNotif>
        </Fade>
      </div>
    </Container>
  );
};

export default Total;
