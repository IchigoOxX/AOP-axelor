import React, { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';
import { Calculator } from '../Common/Calculator';
import { useDispatch, useSelector } from 'react-redux';
import {
    addPayment, addPaymentThunk, fetchPaymentModes,
    removePayment,
    resetPayments,
    selectPaymentMode,
    updatePaymentAmount,
} from '../Redux/paymentSlice';
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined';
import { useNavigate } from 'react-router-dom';
import { Alert, Fade } from '@mui/material';
import { clearCart } from '../Redux/cartSlice';
import {
    deleteTicketThunk,
    completeSaleOrderThunk,
} from '../Redux/ticketSlice';
import {fetchShiftsThunk, updateShiftAfterSaleThunk, updateShiftPayments} from '../Redux/shiftSlice';
import PrintReceipt from '../Orders/PrintReceipt';

const Container = styled.div`
  height: calc(100% - 40px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-left: solid 1px #ccc;
  border-right: solid 1px #ccc;
  overflow: hidden;
`;
const Total = styled.div`
  & > * {
    display: flex;
    justify-content: space-between;
    font-size: 20px;
    font-weight: bolder;
  }
`;
const Payments = styled.div`
  margin: 15px 0;
  /* flex: 1; */
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  row-gap: 10px;
`;
const Payment = styled.div`
  margin-right: 5px;
  padding: 5px 10px;
  display: flex;
  justify-content: space-between;
  row-gap: 10px;
  border: solid gray 1px;
  border-radius: 12px;
  border-color: ${props => (props.isSelected ? '#a8d0e7' : 'gray')};
  border-width: ${props => (props.isSelected ? '2px' : '1px')};
`;
const Amount = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;
const Buttons = styled.div`
  display: flex;
  gap: 10px;
  margin-top: 10px;
`;
const Button = styled.button`
  flex: 1;
  padding: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #0c085c;
  background-color: #a8d0e7;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  transition: color 0.3s;
  box-shadow: 0 1px 2px 1px rgba(0, 0, 0, 0.4);
  &:hover {
    background-color: #54528c;
    color: #a8d0e7;
  }
`;
const AlertNotif = styled(Alert)`
  position: absolute;
  top: 20px;
  right: 42%;
  z-index: 10;
`;

const MiddlePanel = props => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [printPopup, setPrintPopup] = useState(false);
  const [showAlert, setShowAlert] = useState(false);
  const total = useSelector(state => state.payment.total);
  const paidAmount = useSelector(state => state.payment.paidAmount);
  const remainingAmount = useSelector(state => state.payment.remainingAmount);
  const changeAmount = useSelector(state => state.payment.changeAmount);
  const payments = useSelector(state => state.payment.payments);
  const selectedPayment = payments.find(payment => payment.isSelected);
  const currentShift = useSelector(state => state.shifts.currentShift)

  // useEffect(() => {
  //   console.log('payments: ', payments);
  // }, [payments]);

  const currentTicket = useSelector(state => state.cart.currentTicket);
 console.log("ticket in here: ", currentTicket)
  const handleCalculatorInput = key => {
    if (!selectedPayment) return;

    let newAmount;
    if (key === 'C') {
      newAmount = 0;
    } else {
      const currentAmount = selectedPayment.amount.toString();
      newAmount = parseFloat(currentAmount + key.toString());
    }
    dispatch(updatePaymentAmount({ mode: selectedPayment.mode, amount: newAmount }));
  };

  const handleSelectPaymentMode = mode => {
    const existingPayment = payments.find(payment => payment.mode === mode);
    if (existingPayment) {
      dispatch(selectPaymentMode(mode));
    } else {
      dispatch(selectPaymentMode(mode));
      dispatch(addPayment({ mode, amount: 0 }));

    }
  };

  const handleRemovePayment = index => {
    dispatch(removePayment(index));
  };

  const handlePay = () => {
    if (paidAmount >= total) {
      setPrintPopup(true);
    } else {
      setShowAlert(true);
      setTimeout(() => setShowAlert(false), 1500);
    }
  };
  const handlePrint = () => {
    setPrintPopup(false);
    // dispatch(updateTicketThunk({ saleOrderId: ticket.id, statusSelect: 4 }));
    dispatch(updateShiftPayments({ amount: total }));
    dispatch(completeSaleOrderThunk(currentTicket.id));
    payments.forEach(payment => {
        dispatch(addPaymentThunk(payment));
    })
    dispatch(updateShiftAfterSaleThunk({shiftId: currentShift.id,saleOrderId: currentTicket.id}))
    navigate('/');
    dispatch(clearCart());
    dispatch(resetPayments());
  };
  const handleCancel = () => {
    dispatch(deleteTicketThunk(currentTicket.id));
    navigate('/');
    dispatch(clearCart());
    dispatch(resetPayments());
  };
  const handlePrintCancel = () => {
    setPrintPopup(false);
  };

  return (
    <Container>
      <Total>
        <div>
          <div>Total :</div>
          <div>{(parseFloat(total)).toFixed(3)} DT</div>
        </div>
        <div>
          <div>Paid :</div>
          <div>{paidAmount} DT</div>
        </div>
        <div>
          <div>Remaining :</div>
          <div>{remainingAmount} DT</div>
        </div>
        <div>
          <div>Change :</div>
          <div>{changeAmount} DT</div>
        </div>
      </Total>
      <Payments>
        {payments.map((payment, index) => (
          <Payment
            key={index}
            isSelected={payment.isSelected}
            onClick={() => handleSelectPaymentMode(payment.method)}
          >
            <div>{payment.method} :</div>
            <Amount>
              {payment.amount} DT
              <CloseOutlinedIcon
                fontSize="sm"
                style={{ cursor: 'pointer' }}
                onClick={e => {
                  e.stopPropagation();
                  handleRemovePayment(index);
                }}
              />
            </Amount>
          </Payment>
        ))}
      </Payments>
      <div>
        <Calculator onButtonClick={handleCalculatorInput} />
        <Buttons>
          <Button onClick={handlePay}>Pay</Button>
          <Button onClick={handleCancel}>Cancel</Button>
        </Buttons>
      </div>
      <Fade in={showAlert}>
        <AlertNotif severity="error">Insufficient amount paid!</AlertNotif>
      </Fade>
      {printPopup && (
        <PrintReceipt tickets={[currentTicket]} handlePrint={handlePrint} handlePrintCancel={handlePrintCancel} />
      )}
    </Container>
  );
};

export default MiddlePanel;
