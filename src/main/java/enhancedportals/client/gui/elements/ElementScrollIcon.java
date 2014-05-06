package enhancedportals.client.gui.elements;

import java.util.List;

import enhancedportals.client.gui.BaseGui;

public class ElementScrollIcon extends BaseElement
{
    float currentScroll = 0f;
    boolean isScrolling = false, wasClicking = false;
    int scrollAmount = 0;

    public ElementScrollIcon(BaseGui gui, int x, int y)
    {
        super(gui, x, y, 150, 200);
    }

    @Override
    public void addTooltip(List<String> list)
    {

    }

    @Override
    protected void drawContent()
    {

    }

    @Override
    public void update()
    {
        
    }
}