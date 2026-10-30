import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import AttachMoneyOutlinedIcon from '@mui/icons-material/AttachMoneyOutlined';
import CreditCardOutlinedIcon from '@mui/icons-material/CreditCardOutlined';
import DriveFileRenameOutlineOutlinedIcon from '@mui/icons-material/DriveFileRenameOutlineOutlined';
import CardMembershipOutlinedIcon from '@mui/icons-material/CardMembershipOutlined';
import RedeemOutlinedIcon from '@mui/icons-material/RedeemOutlined';
import { useDispatch, useSelector } from 'react-redux';
import {
  addPayment,
  addPaymentThunk,
  fetchPaymentModes,
  fetchPaymentModesThunk,
  selectPaymentMode
} from '../Redux/paymentSlice';
import { Alert, Fade, TextField } from '@mui/material';
import Popup from '../Common/Popup';
import Process from '../Common/Process';
import {fetchCustomersThunk, updateCustomerThunk} from '../Redux/customerSlice';


const Container = styled.div`
  margin: 0 50px 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  font-size: 20px;
`;

const Box = styled.div`
  display: flex;
  gap: 15px;
  background-color: #a8d0e7;
  padding: 8px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(168, 208, 231, 0.7);
  color: #0c085c;
  font-size: 23px;
  font-weight: bold;
  transition: transform 0.3s ease;
  cursor: pointer;
  &:hover {
    transform: scale(1.08)
  }
`;

const DisabledBox = styled(Box)`
  background-color: #ccc;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  color: #7d7d7d;
  cursor: not-allowed;
  &:hover {
    transform: none;
  }
`;

const AlertNotif = styled(Alert)`
  position: absolute;
  top: 20px;
  right: 42%;
  z-index: 10;
`;

const PopupContent = styled.div`
  display: flex;
  gap: 15px;
`;

const ButtonContainer = styled.div`
  margin-top: 15px;
  display: flex;
  justify-content: center;
  gap: 10px;
`;

const Button = styled.button`
  padding: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #0c085c;
  background-color: #a8d0e7;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.4);
  &:hover {
    background-color: #54528c;
    color: #a8d0e7;
  }
`;


const PaymentModes = props => {
  const dispatch = useDispatch();
  const customer = useSelector(state => state.customers.selectedCustomer);
  const remainingAmount = useSelector(state => state.payment.remainingAmount);
  const paymentModes = useSelector(state => state.payment.paymentModes)
  const [showPoints, setShowPoints] = useState(false);
  const [amountToPay, setAmountToPay] = useState(remainingAmount);
  const [pointsError, setPointsError] = useState(false);
  const [showAlert, setShowAlert] = useState(false);
  const [showCreditCardProcess, setShowCreditCardProcess] = useState(false);
  const [showVoucherProcess, setShowVoucherProcess] = useState(false);
  const [expiredAlert, setExpiredAlert] = useState(false);
  const [showChequeDetails, setShowChequeDetails] = useState(false);
  const [chequeDetails, setChequeDetails] = useState({
    paidAmount: amountToPay || 0,
    chequeNumber: '',
    chequeDepositDate: '',
  });

  const saleOrder = useSelector(state => state.cart.currentTicket);

  useEffect(() => {
    setPointsError(calculatePointsNeeded() > customer?.loyaltyCard?.points);
  }, [amountToPay]);

  const getPaymentModeByType = typeSelect => {
    return paymentModes? paymentModes.find(mode => mode.typeSelect === typeSelect) : {};
  };

  const handleSelectPaymentMode = (mode) => {
    if (!mode) return;
    dispatch(selectPaymentMode(mode));
    dispatch(addPayment({
      paymentMode: {id: mode.id},
      saleOrder,
      partner: saleOrder.clientPartner,
      paidAmount: 0
    }));
    // dispatch(addPaymentThunk({
    //   paymentMode: {id: mode.id},
    //   saleOrder,
    //   partner: saleOrder.clientPartner
    // }));
  };

  const handleSelectCheque = () => {
    setShowChequeDetails(true);
  };
  const handleSelectCreditCard = () => {
    setShowCreditCardProcess(true);
  };

  const handleSelectVoucher = () => {
    setShowVoucherProcess(true);
  };

  const handleSelectLoyaltyCard = () => {
    const expirationDate = new Date(customer.loyaltyCard.expirationDate);
    const currentDate = new Date();
    if (expirationDate < currentDate) {
      setExpiredAlert(true);
      setTimeout(() => setExpiredAlert(false), 1500);
      return;
    }

    if (customer.loyaltyCard.points !== 0) {
      const maxPayableAmount = Math.floor(customer.loyaltyCard.points / 33);
      const initialAmountToPay = remainingAmount > maxPayableAmount ? maxPayableAmount : remainingAmount;
      setAmountToPay(initialAmountToPay);
      setShowPoints(true);
    } else {
      setShowAlert(true);
      setTimeout(() => setShowAlert(false), 1500);
    }
  };

  const calculatePointsNeeded = () => {
    return Math.ceil(amountToPay * 33);
  };

  const handleChange = event => {
    const { name, value } = event.target;
    if (name === 'amountToPay') {
      const paidAmount = parseFloat(value) || 0;
      setAmountToPay(paidAmount);
    } else {
      setChequeDetails(prevDetails => ({
        ...prevDetails,
        [name]: value,
      }));
    }
  };

  const handleConfirmLoyaltyCardPayment = () => {
    if (!pointsError && amountToPay !== 0) {
      const mode = getPaymentModeByType(8)
      dispatch(addPayment({
        paymentMode: {id: mode.id},
        saleOrder,
        partner: saleOrder.clientPartner,
        paidAmount: amountToPay
      }));
      setShowPoints(false);
      const pointsToDeduct = Math.ceil(amountToPay * 33);
      const updatedPoints = customer.loyaltyCard.points - pointsToDeduct;
      dispatch(
          updateCustomerThunk({
            ...customer,
            loyaltyCard: {
              ...customer.loyaltyCard,
              points: updatedPoints,
            },
          }),
      );
    }
  };

  const handleConfirmCheque = () => {
    if (chequeDetails.chequeNumber && chequeDetails.chequeDepositDate) {
      const mode = getPaymentModeByType(7);
      dispatch(
          addPayment({
            paymentMode : {id: mode.id},
            paidAmount: chequeDetails.paidAmount,
            saleOrder,
            partner: saleOrder.clientPartner,
            chequeNumber: chequeDetails.chequeNumber,
            chequeDate: chequeDetails.chequeDepositDate
          })
      );
      // dispatch(addPaymentThunk({
      //   paymentMode : {id: mode.id},
      //   paidAmount: chequeDetails.paidAmount,
      //   saleOrder,
      //   partner: saleOrder.clientPartner,
      //   chequeNumber: chequeDetails.chequeNumber,
      //   chequeDate: chequeDetails.chequeDepositDate
      // }))
      setShowChequeDetails(false);
      setChequeDetails({ chequeNumber: '', chequeDepositDate: '' });
    }
  };

  return (
      <Container>
        <h3>Payment mode</h3>

        {/* Cash Payment */}
        {getPaymentModeByType(5) ? (
            <Box onClick={() => handleSelectPaymentMode(getPaymentModeByType(5))}>
              <AttachMoneyOutlinedIcon fontSize="large" />
              <div>Cash</div>
            </Box>
        ) : (
            <DisabledBox>
              <AttachMoneyOutlinedIcon fontSize="large" />
              <div>Cash</div>
            </DisabledBox>
        )}

        {/* Credit Card Payment */}
        {getPaymentModeByType(6) ? (
            <Box onClick={handleSelectCreditCard}>
              <CreditCardOutlinedIcon fontSize="large" />
              <div>Credit Card</div>
            </Box>
        ) : (
            <DisabledBox>
              <CreditCardOutlinedIcon fontSize="large" />
              <div>Credit Card</div>
            </DisabledBox>
        )}
        {showCreditCardProcess && <Process setShowProcess={setShowCreditCardProcess} />}

        {/* Cheque Payment */}
        {getPaymentModeByType(7) ? (
            <Box onClick={handleSelectCheque}>
              <DriveFileRenameOutlineOutlinedIcon fontSize="large" />
              <div>Cheque</div>
            </Box>
        ) : (
            <DisabledBox>
              <DriveFileRenameOutlineOutlinedIcon fontSize="large" />
              <div>Cheque</div>
            </DisabledBox>
        )}
        {showChequeDetails && (
            <Popup title="Cheque Details" onClose={() => setShowChequeDetails(false)}>
              <PopupContent>
                <TextField
                    label="Amount"
                    id="outlined-size-small"
                    name="amount"
                    value={chequeDetails.paidAmount}
                    onChange={handleChange}
                    size="small"
                />
                <TextField
                    label="Cheque Number"
                    id="outlined-size-small"
                    name="chequeNumber"
                    value={chequeDetails.chequeNumber}
                    onChange={handleChange}
                    size="small"
                />
                <TextField
                    label="Cheque Deposit Date"
                    id="outlined-size-small"
                    name="chequeDepositDate"
                    type="date"
                    value={chequeDetails.chequeDepositDate}
                    onChange={handleChange}
                    size="small"
                    InputLabelProps={{
                      shrink: true,
                    }}
                />
              </PopupContent>
              <ButtonContainer>
                <Button variant="contained" color="primary" onClick={handleConfirmCheque()}>
                  Confirm
                </Button>
              </ButtonContainer>
            </Popup>
        )}

        {/* Loyalty Card Payment */}
        {customer?.loyaltyCard ? (
            <>
              <Box onClick={handleSelectLoyaltyCard}>
                <CardMembershipOutlinedIcon fontSize="large" />
                <div>Loyalty Card</div>
              </Box>
              <Fade in={showAlert}>
                <AlertNotif severity="error">This Loyalty Card Has 0 Points!</AlertNotif>
              </Fade>
              <Fade in={expiredAlert}>
                <AlertNotif severity="error">This Loyalty Card Has Expired!</AlertNotif>
              </Fade>
              {showPoints && (
                  <Popup
                      title="Transfer Points"
                      onClose={() => {
                        setShowPoints(false);
                      }}
                  >
                    <PopupContent>
                      <TextField
                          label="Amount To Pay"
                          id="outlined-size-small"
                          name="amountToPay"
                          value={amountToPay}
                          onChange={handleChange}
                          focused
                          size="small"
                          error={pointsError}
                          helperText={pointsError ? 'Not enough points' : ''}
                      />
                      <TextField
                          label="Available Points"
                          id="outlined-size-small"
                          defaultValue={customer.loyaltyCard.points}
                          size="small"
                          disabled
                      />
                      <TextField
                          label="Points Needed"
                          id="outlined-size-small"
                          value={calculatePointsNeeded()}
                          size="small"
                          disabled
                      />
                    </PopupContent>
                    <ButtonContainer>
                      <Button
                          variant="contained"
                          color="primary"
                          onClick={handleConfirmLoyaltyCardPayment}
                          disabled={pointsError}
                      >
                        Confirm
                      </Button>
                    </ButtonContainer>
                  </Popup>
              )}
            </>
        ) : (
            <DisabledBox>
              <CardMembershipOutlinedIcon fontSize="large" />
              <div>Loyalty Card</div>
            </DisabledBox>
        )}

        {/* Voucher Payment */}
        {getPaymentModeByType(1) ? (
            <Box onClick={handleSelectVoucher}>
              <RedeemOutlinedIcon fontSize="large" />
              <div>Voucher</div>
            </Box>
        ) : (
            <DisabledBox>
              <RedeemOutlinedIcon fontSize="large" />
              <div>Voucher</div>
            </DisabledBox>
        )}
        {showVoucherProcess && <Process setShowProcess={setShowVoucherProcess} />}
      </Container>
  );
};

export default PaymentModes;