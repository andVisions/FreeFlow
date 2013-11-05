package org.freeflow.layouts;

import java.util.HashMap;

import org.freeflow.core.BaseSectionedAdapter;
import org.freeflow.core.Frame;
import org.freeflow.core.FrameDescriptor;
import org.freeflow.core.LayoutController;
import org.freeflow.core.Section;

public class VLayout extends LayoutController {

	private static final String TAG = "VLayout";
	private int itemHeight = -1;
	private int width = -1;
	private int height = -1;
	private BaseSectionedAdapter itemsAdapter;
	private HashMap<Object, FrameDescriptor> frameDescriptors = new HashMap<Object, FrameDescriptor>();
	private int headerHeight = -1;
	private int headerWidth = -1;

	public void setItemHeight(int i) {
		this.itemHeight = i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDimensions(int measuredWidth, int measuredHeight) {
		if (measuredHeight == height && measuredWidth == width) {
			return;
		}
		this.width = measuredWidth;
		this.height = measuredHeight;
		if (itemsAdapter != null) {
			generateFrameDescriptors();
		}
	}

	@Override
	public void setItems(BaseSectionedAdapter adapter) {
		this.itemsAdapter = adapter;

		if (width != -1 && height != -1) {
			generateFrameDescriptors();
		}
	}

	/**
	 * TODO: Future optimization: can we avoid object allocation here?
	 */
	@Override
	public void generateFrameDescriptors() {
		if (itemHeight < 0) {
			throw new IllegalStateException("itemHeight not set");
		}

		if (height < 0 || width < 0) {
			throw new IllegalStateException("dimensions not set");
		}
		
		if (headerWidth < 0) {
			throw new IllegalStateException("headerWidth not set");
		}
		
		if (headerHeight < 0) {
			throw new IllegalStateException("headerHeight not set");
		}


		frameDescriptors.clear();
		int topStart = 0;

		for (int i = 0; i < itemsAdapter.getSectionCount(); i++) {

			FrameDescriptor header = new FrameDescriptor();
			Frame hframe = new Frame();
			header.itemSection = i;
			header.itemIndex = -1;
			header.isHeader = true;
			hframe.left = 0;
			hframe.top = topStart;
			hframe.width = headerWidth;
			hframe.height = headerHeight;
			header.frame = hframe;
			header.data = itemsAdapter.getSection(i).getSectionTitle();
			frameDescriptors.put(header.data, header);

			topStart += headerHeight;

			for (int j = 0; j < itemsAdapter.getSectionCount(); j++) {
				FrameDescriptor descriptor = new FrameDescriptor();
				Frame frame = new Frame();
				descriptor.itemSection = i;
				descriptor.itemIndex = j;
				frame.left = 0;
				frame.top = j * itemHeight + topStart;
				frame.width = width;
				frame.height = itemHeight;
				descriptor.frame = frame;
				descriptor.data = itemsAdapter.getItem(i, j);
				frameDescriptors.put(descriptor.data, descriptor);
			}

			topStart += (itemsAdapter.getCountForSection(i)) * itemHeight;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HashMap<Object, FrameDescriptor> getFrameDescriptors(int viewPortLeft, int viewPortTop) {
		HashMap<Object, FrameDescriptor> desc = new HashMap<Object, FrameDescriptor>();

		Object[] keyset = frameDescriptors.keySet().toArray();
		for (int i = 0; i < frameDescriptors.size(); i++) {
			FrameDescriptor fd = frameDescriptors.get(keyset[i]);
			if (fd.frame.top + itemHeight > viewPortTop && fd.frame.top < viewPortTop + height) {
				FrameDescriptor newDesc = FrameDescriptor.clone(fd);
				newDesc.frame.top -= viewPortTop;
				desc.put(newDesc.data, newDesc);
			}
		}

		return desc;
	}

	@Override
	public Frame getViewportFrameForItem(Object item) {
		FrameDescriptor fd = frameDescriptors.get(item);

		Frame frame = new Frame();
		frame.top = fd.frame.top;
		frame.left = 0;
		frame.width = width;
		frame.height = height;

		if (frame.top > getMaximumViewPortY())
			frame.top = getMaximumViewPortY();

		return frame;
	}

	@Override
	public Frame getOffScreenStartFrame() {
		Frame frame = new Frame();
		frame.height = itemHeight;
		frame.width = width;
		frame.left = 0;
		frame.top = height;

		return frame;
	}

	@Override
	public boolean horizontalDragEnabled() {
		return false;
	}

	@Override
	public boolean verticalDragEnabled() {
		return true;
	}

	@Override
	public int getMinimumViewPortX() {
		return 0;
	}

	@Override
	public int getMinimumViewPortY() {
		return 0;
	}

	@Override
	public int getMaximumViewPortX() {
		return width;
	}

	@Override
	public int getMaximumViewPortY() {
		if (itemsAdapter == null)
			return 0;

		int sectionIndex = itemsAdapter.getSectionCount() - 1;
		Section s = itemsAdapter.getSection(sectionIndex);

		Object lastFrameData = itemsAdapter.getItem(sectionIndex, s.getDataCount() - 1);
		FrameDescriptor fd = frameDescriptors.get(lastFrameData);

		return (fd.frame.top + fd.frame.height) - height;
	}

	@Override
	public FrameDescriptor getFrameDescriptorForItemAndViewport(Object data, int viewPortLeft, int viewPortTop) {
		FrameDescriptor fd = FrameDescriptor.clone(frameDescriptors.get(data));

		fd.frame.left -= viewPortLeft;
		fd.frame.top -= viewPortTop;

		return fd;
	}
	
	@Override
	public void setHeaderItemDimensions(int hWidth, int hHeight) {
		headerWidth = hWidth;
		headerHeight = hHeight;
	}

}
