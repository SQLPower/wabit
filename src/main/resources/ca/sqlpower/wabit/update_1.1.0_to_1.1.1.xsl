<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform 
     version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="olap-report-query">
    <olap-query>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
    </olap-query>
</xsl:template>

<xsl:template match="olap-report-cube">
    <olap-cube><xsl:apply-templates select="@*"/></olap-cube>
</xsl:template>

<xsl:template match="olap4j-report-query">
    <olap4j-query>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
    </olap4j-query>
</xsl:template>

<xsl:template match="olap4j-report-axis">
    <olap4j-axis>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
    </olap4j-axis>
</xsl:template>

<xsl:template match="olap4j-report-dimension">
    <olap4j-dimension>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
    </olap4j-dimension>
</xsl:template>

<xsl:template match="olap4j-report-selection">
    <olap4j-selection>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
    </olap4j-selection>
</xsl:template>

  <!-- XSLT Template to copy anything, priority="-1" -->
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>