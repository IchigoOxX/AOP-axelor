import React from 'react';
import styled from 'styled-components';

const Section = styled.div`
  display: grid;
  grid-template-columns: 1fr 2.5fr;
  border: solid #a8d0e7 1px;
  border-radius: 10px;
  box-shadow: 0 2px 3px 0 rgba(168, 208, 231, 0.7);
`;
const SectionTitle = styled.h3`
  display: flex;
  align-items: center;
  text-align: center;
  gap: 8px;
  border-right: solid #a8d0e7 1.5px;
  padding: 10px 20px;
  & > div {
    padding-top: 5px;
  }
`;
const SectionDetails = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 10px;
  & > * {
    display: grid;
    grid-template-columns: 1fr 3fr;
    font-size: 16px;
  }
`;

const SectionComponent = ({ title, icon, children }) => {
  return (
    <Section>
      <SectionTitle>
        <div>{icon}</div>
        {title}
      </SectionTitle>
      <SectionDetails>{children}</SectionDetails>
    </Section>
  );
};

export default SectionComponent;
