import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import styled from "styled-components";
import Page from "../Common/Page";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";

const Container = styled.div`
  padding: 0 30px 10px 30px;
`;
const Title = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 18px;
  color: #0c085c;
`;
const Content = styled.div`
  margin: 20px 50px;
`;

const DetailsContainer = styled.div`
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  padding: 20px;
  margin: 20px 0;
`;

const SectionHeader = styled.div`
  display: flex;
  align-items: center;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
  margin-bottom: 20px;
`;

const SectionTitle = styled.h2`
  font-size: 24px;
  color: #333;
  margin: 0;
`;

const SectionBody = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
`;

const DetailsWrapper = styled.div`
  display: flex;
  flex-direction: column;
`;

const DetailsGroup = styled.div`
  margin-bottom: 20px;
`;

const DetailTitle = styled.div`
  font-size: 16px;
  color: #888;
`;

const DetailValue = styled.div`
  font-size: 18px;
  color: #333;
  font-weight: 500;
`;

const ProductImage = styled.img`
  max-width: 250px;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
`;

const ProductDetailsPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { product } = location.state;

  console.log('product is: ', product.name)

  if (!product) {
    return <div>Product details not found.</div>;
  }

  const handleGoBack = () => {
    navigate("/items");
  };

  return (
      <Page>
        <Container>
          <Title>
            <ArrowBackOutlinedIcon
                fontSize="large"
                style={{ cursor: "pointer" }}
                onClick={handleGoBack}
            />
            <h2>{product.name}</h2>
          </Title>
          <Content>
            <DetailsContainer>
              <SectionHeader>
                <SectionTitle>Product Details</SectionTitle>
              </SectionHeader>
              <SectionBody>
                <DetailsWrapper>
                  <DetailsGroup>
                    <DetailTitle>Product Name</DetailTitle>
                    <DetailValue>{product.name}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Product Category</DetailTitle>
                    <DetailValue>{product.productCategory.name}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Product Family</DetailTitle>
                    <DetailValue>{product.productFamily.name}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Description</DetailTitle>
                    <DetailValue>{product.description}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Reference</DetailTitle>
                    <DetailValue>{product.code}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Barcode</DetailTitle>
                    <DetailValue>{product.barCode}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Unit</DetailTitle>
                    <DetailValue>{product.salesUnit?.name}</DetailValue>
                  </DetailsGroup>
                </DetailsWrapper>
                <ProductImage src={product.imageUrl} alt={product.name} />
              </SectionBody>
              <SectionHeader>
                <SectionTitle>Financials</SectionTitle>
              </SectionHeader>
              <SectionBody>
                <DetailsWrapper>
                  <DetailsGroup>
                    <DetailTitle>Price Excl. Tax</DetailTitle>
                    <DetailValue></DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Tax Rate</DetailTitle>
                    <DetailValue></DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Cost</DetailTitle>
                    <DetailValue>{product.costPrice}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Margin</DetailTitle>
                    <DetailValue></DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Discount</DetailTitle>
                    <DetailValue></DetailValue>
                  </DetailsGroup>
                </DetailsWrapper>
              </SectionBody>
              <SectionHeader>
                <SectionTitle>Inventory</SectionTitle>
              </SectionHeader>
              <SectionBody>
                <DetailsWrapper>
                  <DetailsGroup>
                    <DetailTitle>Remaining in Stock</DetailTitle>
                    <DetailValue>{product.stock}</DetailValue>
                  </DetailsGroup>
                  <DetailsGroup>
                    <DetailTitle>Sold</DetailTitle>
                    <DetailValue>{product.totalSold}</DetailValue>
                  </DetailsGroup>
                </DetailsWrapper>
              </SectionBody>
            </DetailsContainer>
          </Content>
        </Container>
      </Page>
  );
};

export default ProductDetailsPage;
