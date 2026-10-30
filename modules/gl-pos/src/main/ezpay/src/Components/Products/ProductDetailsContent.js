import React from "react";
import styled from "styled-components";

const Container = styled.div`
  padding: 0 30px 10px 30px;
`;

const Section = styled.div`
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
  max-width: 200px;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
`;

const ProductDetailsContent = ({ product }) => {
  if (!product) {
    return <div>Product details not found.</div>;
  }

  return (
      <Container>
        {/* Product Details Section */}
        <Section>
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
                <DetailValue>{product.category?.name}</DetailValue>
              </DetailsGroup>
              <DetailsGroup>
                <DetailTitle>Product Family</DetailTitle>
                <DetailValue>{product.family?.name}</DetailValue>
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
                <DetailValue>{product.barcode}</DetailValue>
              </DetailsGroup>
              <DetailsGroup>
                <DetailTitle>Unit</DetailTitle>
                <DetailValue>{product.unit?.name}</DetailValue>
              </DetailsGroup>
            </DetailsWrapper>
            <ProductImage src={product.imageUrl} alt={product.name} />
          </SectionBody>
        </Section>

        {/* Financials Section */}
        <Section>
          <SectionHeader>
            <SectionTitle>Financials</SectionTitle>
          </SectionHeader>
          <SectionBody>
            <DetailsWrapper>
              <DetailsGroup>
                <DetailTitle>Price Excl. Tax</DetailTitle>
                <DetailValue>{product.priceExclTax}</DetailValue>
              </DetailsGroup>
              <DetailsGroup>
                <DetailTitle>Tax Rate</DetailTitle>
                <DetailValue>{product.taxRate}</DetailValue>
              </DetailsGroup>
              <DetailsGroup>
                <DetailTitle>Cost</DetailTitle>
                <DetailValue>{product.cost}</DetailValue>
              </DetailsGroup>
              <DetailsGroup>
                <DetailTitle>Margin</DetailTitle>
                <DetailValue>{product.margin}</DetailValue>
              </DetailsGroup>
              <DetailsGroup>
                <DetailTitle>Discount</DetailTitle>
                <DetailValue>{product.discount}</DetailValue>
              </DetailsGroup>
            </DetailsWrapper>
          </SectionBody>
        </Section>

        {/* Inventory Section */}
        <Section>
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
        </Section>
      </Container>
  );
};

export default ProductDetailsContent;
