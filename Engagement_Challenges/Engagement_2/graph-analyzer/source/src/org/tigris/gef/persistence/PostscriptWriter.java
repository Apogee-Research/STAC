package org.tigris.gef.persistence;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.OutputStream;

/**
 * since v0.12.4. Use org.tigris.gef.persistence.export.PostScriptWriter
 * instead.
 */
public class PostscriptWriter extends ScalableGraphics {

    private org.tigris.gef.persistence.export.PostscriptWriter psWriter;

    public PostscriptWriter(String filename) throws IOException {
        psWriter = new org.tigris.gef.persistence.export.PostscriptWriter(
                filename);
    }

    public PostscriptWriter(String filename, Rectangle boundingBox)
            throws IOException {
        psWriter = new org.tigris.gef.persistence.export.PostscriptWriter(
                filename, boundingBox);
    }

    public PostscriptWriter(OutputStream stream) throws IOException {
        psWriter = new org.tigris.gef.persistence.export.PostscriptWriter(
                stream);

    }

    public PostscriptWriter(OutputStream stream, Rectangle bb)
            throws IOException {
        psWriter = new org.tigris.gef.persistence.export.PostscriptWriter(
                stream, bb);
    }

    public Graphics create() {
        return psWriter.create();
    }

    public Graphics create(int x, int y, int width, int height) {
        return psWriter.create(x, y, width, height);
    }

    public void dispose() {
        psWriter.dispose();
    }

    public void setColorConversion(Color source, Color target) {
        psWriter.setColorConversion(source, target);
    }

    public Color getColor() {
        return psWriter.getColor();
    }

    public void setColor(Color c) {
        psWriter.setColor(c);
    }

    public void setPaintMode() {
        psWriter.setPaintMode();
    }

    public void setXORMode(Color otherColor) {
        psWriter.setXORMode(otherColor);
    }

    public Font getFont() {
        return psWriter.getFont();
    }

    public void setFont(Font font) {
        psWriter.setFont(font);
    }

    public FontMetrics getFontMetrics() {
        return psWriter.getFontMetrics();
    }

    public FontMetrics getFontMetrics(Font font) {
        return psWriter.getFontMetrics(font);
    }

    public java.awt.Rectangle getClipBounds() {
        return psWriter.getClipBounds();
    }

    public void clipRect(int x, int y, int w, int h) {
        psWriter.clipRect(x, y, w, h);
    }

    public Shape getClip() {
        return psWriter.getClip();
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        psWriter.copyArea(x, y, width, height, dx, dy);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return psWriter.drawImage(img, x, y, observer);
    }

    public boolean drawImage(Image img, int x, int y, int w, int h,
            ImageObserver observer) {
        return psWriter.drawImage(img, x, y, w, h, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return psWriter.drawImage(img, x, y, bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return psWriter.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return psWriter.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
                observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        return psWriter.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
                bgcolor, observer);
    }

    public void drawRect(int x, int y, int w, int h) {
        psWriter.drawRect(x, y, w, h);
    }

    public void fillRect(int x, int y, int w, int h) {
        psWriter.fillRect(x, y, w, h);
    }

    public void clearRect(int x, int y, int w, int h) {
        psWriter.clearRect(x, y, w, h);
    }

    public void drawOval(int x, int y, int w, int h) {
        psWriter.drawOval(x, y, w, h);
    }

    public void fillOval(int x, int y, int w, int h) {
        psWriter.fillOval(x, y, w, h);
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        psWriter.drawArc(x, y, w, h, startAngle, arcAngle);
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        psWriter.fillArc(x, y, w, h, startAngle, arcAngle);
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        psWriter.drawRoundRect(x, y, w, h, arcw, arch);
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        psWriter.fillRoundRect(x, y, w, h, arcw, arch);
    }

    public void writePolygonPath(int xPoints[], int yPoints[], int nPoints) {
        psWriter.writePolygonPath(xPoints, yPoints, nPoints);
    }

    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        psWriter.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawPolygon(Polygon poly) {
        psWriter.drawPolygon(poly);
    }

    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        psWriter.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void fillPolygon(Polygon poly) {
        psWriter.fillPolygon(poly);
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        psWriter.drawPolyline(xPoints, yPoints, nPoints);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        psWriter.drawLine(x1, y1, x2, y2);
    }

    public void setClip(int x, int y, int w, int h) {
        psWriter.setClip(x, y, w, h);
    }

    public void setClip(Shape clip) {
        psWriter.setClip(clip);
    }

    public void translate(int x, int y) {
        psWriter.translate(x, y);
    }

    public void scale(double xscale, double yscale) {
        psWriter.scale(xscale, yscale);
    }

    public void drawString(String text, int x, int y) {
        psWriter.drawString(text, x, y);
    }

    public void comment(String cmt) {
        psWriter.comment(cmt);
    }

    // For Java 1.2 uncomment the following lines:
    //
    public void drawString(java.text.AttributedCharacterIterator iterator,
            int i, int j) {
        psWriter.drawString(iterator, i, j);
    }

    public void addRenderingHints(java.util.Map map) {
        psWriter.addRenderingHints(map);
    }

    public void clip(java.awt.Shape shape) {
        psWriter.clip(shape);
    }

    public void draw(java.awt.Shape shape) {
        psWriter.draw(shape);
    }

    public void drawGlyphVector(java.awt.font.GlyphVector glyphVector,
            float param, float param2) {
        psWriter.drawGlyphVector(glyphVector, param, param2);
    }

    public boolean drawImage(java.awt.Image image,
            java.awt.geom.AffineTransform affineTransform,
            java.awt.image.ImageObserver imageObserver) {
        return psWriter.drawImage(image, affineTransform, imageObserver);
    }

    public void drawImage(java.awt.image.BufferedImage bufferedImage,
            java.awt.image.BufferedImageOp bufferedImageOp, int param,
            int param3) {
        psWriter.drawImage(bufferedImage, bufferedImageOp, param, param3);
    }

    public void drawRenderableImage(
            java.awt.image.renderable.RenderableImage renderableImage,
            java.awt.geom.AffineTransform affineTransform) {
        psWriter.drawRenderableImage(renderableImage, affineTransform);
    }

    public void drawRenderedImage(java.awt.image.RenderedImage renderedImage,
            java.awt.geom.AffineTransform affineTransform) {
        psWriter.drawRenderedImage(renderedImage, affineTransform);
    }

    public void drawString(
            java.text.AttributedCharacterIterator attributedCharacterIterator,
            float param, float param2) {
        psWriter.drawString(attributedCharacterIterator, param, param2);
    }

    public void drawString(String str, float param, float param2) {
        psWriter.drawString(str, param, param2);
    }

    public void fill(java.awt.Shape shape) {
        psWriter.fill(shape);
    }

    public java.awt.Color getBackground() {
        return psWriter.getBackground();
    }

    public java.awt.Composite getComposite() {
        return psWriter.getComposite();
    }

    public java.awt.GraphicsConfiguration getDeviceConfiguration() {
        return psWriter.getDeviceConfiguration();
    }

    public java.awt.font.FontRenderContext getFontRenderContext() {
        return psWriter.getFontRenderContext();
    }

    public java.awt.Paint getPaint() {
        return psWriter.getPaint();
    }

    public Object getRenderingHint(java.awt.RenderingHints.Key key) {
        return psWriter.getRenderingHint(key);
    }

    public java.awt.RenderingHints getRenderingHints() {
        return psWriter.getRenderingHints();
    }

    public java.awt.Stroke getStroke() {
        return psWriter.getStroke();
    }

    public java.awt.geom.AffineTransform getTransform() {
        return psWriter.getTransform();
    }

    public boolean hit(java.awt.Rectangle rectangle, java.awt.Shape shape,
            boolean param) {
        return psWriter.hit(rectangle, shape, param);
    }

    public void rotate(double param) {
        psWriter.rotate(param);
    }

    public void rotate(double param, double param1, double param2) {
        psWriter.rotate(param, param1, param2);
    }

    public void setBackground(java.awt.Color color) {
        psWriter.setBackground(color);
    }

    public void setComposite(java.awt.Composite composite) {
        psWriter.setComposite(composite);
    }

    public void setPaint(java.awt.Paint paint) {
        psWriter.setPaint(paint);
    }

    public void setRenderingHint(java.awt.RenderingHints.Key key, Object obj) {
        psWriter.setRenderingHint(key, obj);
    }

    public void setRenderingHints(java.util.Map map) {
        psWriter.setRenderingHints(map);
    }

    public void setStroke(java.awt.Stroke stroke) {
        psWriter.setStroke(stroke);
    }

    public void setTransform(java.awt.geom.AffineTransform affineTransform) {
        psWriter.setTransform(affineTransform);
    }

    public void shear(double param, double param1) {
        psWriter.shear(param, param1);
    }

    public void transform(java.awt.geom.AffineTransform affineTransform) {
        psWriter.transform(affineTransform);
    }

    public void translate(double param, double param1) {
        psWriter.translate(param, param1);
    }

    @Override
    public void setScale(double s) {
        psWriter.setScale(s);
    }

}
