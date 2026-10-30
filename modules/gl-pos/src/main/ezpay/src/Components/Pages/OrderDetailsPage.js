import React, { useState } from "react";
import OrderContent from "../Orders/OrderContent";
import { useLocation, useNavigate } from "react-router-dom";
import Page from "../Common/Page";
import styled from "styled-components";
import ButtonComponent from "../Common/ButtonComponent";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import LocalPrintshopOutlinedIcon from "@mui/icons-material/LocalPrintshopOutlined";
import CurrencyExchangeOutlinedIcon from "@mui/icons-material/CurrencyExchangeOutlined";
import PrintReceipt from "../Orders/PrintReceipt";
import { useSelector } from "react-redux";
import { selectTicketByNumber } from "../Redux/ticketSlice";

const Container = styled.div`
  padding: 0 30px 20px 30px;
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
const Content = styled.div`
  margin: 0 50px;
`;

const OrderDetailsPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { saleOrderSeq = null, mode = null } = location.state;
  const order = useSelector(state => selectTicketByNumber(state, saleOrderSeq));
  const [printPopup, setPrintPopup] = useState(false);

  console.log("receipt order: ", order)
  const handleGoBack = () => {
    navigate("/orders");
  };
  const handleRefund = () => {
    navigate("/orders/order-details", {
      state: { saleOrderSeq, mode: "refund" },
    });
  };
  const handlePrint = () => {
    setPrintPopup(true);
  };

  const handleAfterPrint = () => {
    setPrintPopup(false);
  };
  return (
    <Page>
      <Container>
        <Header>
          <Title>
            <ArrowBackOutlinedIcon
              fontSize="large"
              style={{ cursor: "pointer" }}
              onClick={handleGoBack}
            />
            <h2>Ticket Details</h2>
          </Title>
          {mode === "view" && (
            <Buttons>
              <ButtonComponent
                title="Refund"
                icon={<CurrencyExchangeOutlinedIcon fontSize="sm" />}
                handleClick={() => handleRefund(order)}
              />
              <ButtonComponent
                title="Print"
                icon={<LocalPrintshopOutlinedIcon fontSize="sm" />}
                handleClick={handlePrint}
              />
              {printPopup && (
                <PrintReceipt
                  tickets={[order]}
                  handlePrint={handleAfterPrint}
                  handlePrintCancel={handleAfterPrint}
                />
              )}
            </Buttons>
          )}
        </Header>
        <Content>
          <OrderContent saleOrderSeq={saleOrderSeq} mode={mode} />
        </Content>
      </Container>
    </Page>
  );
};

export default OrderDetailsPage;
