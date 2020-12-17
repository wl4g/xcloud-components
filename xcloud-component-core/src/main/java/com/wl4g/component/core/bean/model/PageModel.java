/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.component.core.bean.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import io.swagger.annotations.ApiParam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

import static com.wl4g.component.common.lang.Assert2.isInstanceOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * The original intention of the integrated paging packaging model is that
 * multiple modules under microservices must be completely decoupled. Therefore,
 * we refer to part of the code of 11 instead of relying on it directly. We are
 * very grateful for {@link com.github.pagehelper.Page} work and fully abide by
 * your agreements.
 * 
 * @auhtor Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018年9月7日
 * @since
 * @see https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md
 */
@ApiModel("Pagination query result")
public class PageModel<E> implements Serializable {
	private static final long serialVersionUID = -7002775417254397561L;

	/**
	 * Page of {@link Page}
	 */
	@JsonIgnore(true)
	private Page<E> page;

	/**
	 * Page record rows.</br>
	 * </br>
	 * 
	 * <b>Note:</b> The following annotation combination configuration does not
	 * implement the effect that the records field can make the request display
	 * no two responses. Can't swagger2 still achieve this effect: when the type
	 * of request and response are the same model class, can't some fields be
	 * displayed or not displayed according to the request and response? </br>
	 * </br>
	 * 
	 * <p>
	 * for negative examples:
	 * 
	 * <pre>
	 * &#64;ApiOperation(value = "Query myuser page list")
	 * &#64;RequestMapping(value = "/list", method = { GET })
	 * public RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; list(PageModel&lt;MyUserModel&gt; pm, MyUserModel param) {
	 * 	RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; resp = RespBase.create();
	 * 	resp.setData(myUserService.page(pm, param));
	 * 	return resp;
	 * }
	 * </pre>
	 * 
	 * for positive examples(Solution):
	 * 
	 * <pre>
	 * &#64;ApiOperation(value = "Query myuser page list")
	 * &#64;ApiImplicitParams({
	 *	&#64;ApiImplicitParam(name = "pageNum", dataType = "int32", defaultValue = "1"),
	 *	&#64;ApiImplicitParam(name = "pageSize", dataType = "int32", defaultValue = "10")
	 * })
	 * &#64;RequestMapping(value = "/list", method = { GET })
	 * public RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; list({@code @ApiIgnore} PageModel&lt;MyUserModel&gt; pm, MyUserModel param) {
	 * 	RespBase&lt;PageModel&lt;MyUserModel&gt;&gt; resp = RespBase.create();
	 * 	resp.setData(myUserService.page(pm, param));
	 * 	return resp;
	 * }
	 * </pre>
	 * </p>
	 */
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private List<E> records = emptyList();

	public PageModel() {
		this(1, 10);
	}

	public PageModel(Integer pageNum, Integer pageSize) {
		setPageNum(pageNum);
		setPageSize(pageSize);
	}

	public Integer getPageNum() {
		return forPage().getPageNum();
	}

	public void setPageNum(Integer pageNum) {
		if (nonNull(pageNum)) {
			forPage().setPageNum(pageNum);
		}
	}

	public Integer getPageSize() {
		return forPage().getPageSize();
	}

	public void setPageSize(Integer pageSize) {
		if (nonNull(pageSize)) {
			forPage().setPageSize(pageSize);
		}
	}

	public Long getTotal() {
		return forPage().getTotal();
	}

	public void setTotal(Long total) {
		if (nonNull(total)) {
			forPage().setTotal(total);
		}
	}

	public Integer getPages() {
		return forPage().getPages();
	}

	public void setPages(Integer pages) {
		if (nonNull(pages)) {
			forPage().setPages(pages);
		}
	}

	public List<E> getRecords() {
		return records;
	}

	public void setRecords(List<E> records) {
		if (!isNull(records)) {
			this.records = records;
		}
	}

	/**
	 * Sets pagination information. </br>
	 * 
	 * In order to adapt to multi module decoupling in microservices, the super
	 * class serializable class receiver is used here
	 * {@link com.github.pagehelper.Page}
	 * 
	 * @param mustGithubHelperPage
	 */
	@SuppressWarnings("unchecked")
	public synchronized void page(@NotNull Serializable mustGithubHelperPage) {
		notNullOf(mustGithubHelperPage, "mustGithubHelperPage");
		isInstanceOf(PAGE_CLASS, mustGithubHelperPage, UnsupportedOperationException.class, "mustGithubHelperPage");

		// Copy pagination metadata
		BeanUtils.copyProperties(mustGithubHelperPage, this.page);

		// Copy pagination data records
		List<E> res = (List<E>) mustGithubHelperPage;
		this.records = (this.records == emptyList()) ? new ArrayList<>(res.size()) : this.records;
		this.records.addAll(res);
	}

	/**
	 * Ensure for get page.
	 * 
	 * @return
	 */
	private Page<E> forPage() {
		if (isNull(page)) {
			synchronized (this) {
				if (isNull(page)) {
					return (this.page = new Page<>());
				}
			}
		}
		return page;
	}

	@Override
	public String toString() {
		return getClass() + " [pageNum=" + getPageNum() + ", pageSize=" + getPageSize() + ", total=" + getTotal() + ", records="
				+ getRecords().size() + "]";
	}

	/**
	 * Mybatis pagination. </br>
	 * Thank you very much {@link com.github.pagehelper.Page}. We are in full
	 * compliance with your agreements.
	 * 
	 * @param <E>
	 * @see http://git.oschina.net/free/Mybatis_PageHelper
	 */
	public static class Page<E> extends ArrayList<E> {
		private static final long serialVersionUID = 1L;

		/** Page number, starting from 1 */
		private int pageNum;
		/** Page size. */
		private int pageSize;
		/** Start row. */
		private int startRow;
		/** End row. */
		private int endRow;
		/** Total row count. */
		private long total;
		/** Total page size. */
		private int pages;
		/** Include count query. */
		private boolean count;
		/**
		 * Count signal: in three cases, the default boundsql is executed when
		 * null, count is executed when true, and paging is performed when
		 * false.
		 */
		private Boolean countSignal;
		/** Sort of sql. */
		private String orderBy;
		/** Add sort only. */
		private boolean orderByOnly;
		/** Paging rationalization. */
		private Boolean reasonable;
		/**
		 * When set to true, if PageSize is set to 0 (or rowbounds limit = 0),
		 * paging is not performed and all results are returned.
		 */
		private Boolean pageSizeZero;

		public Page() {
			super();
		}

		public Page(int pageNum, int pageSize) {
			this(pageNum, pageSize, true, null);
		}

		public Page(int pageNum, int pageSize, boolean count) {
			this(pageNum, pageSize, count, null);
		}

		private Page(int pageNum, int pageSize, boolean count, Boolean reasonable) {
			super(0);
			if (pageNum == 1 && pageSize == Integer.MAX_VALUE) {
				pageSizeZero = true;
				pageSize = 0;
			}
			this.pageNum = pageNum;
			this.pageSize = pageSize;
			this.count = count;
			calculateStartAndEndRow();
			setReasonable(reasonable);
		}

		/**
		 * int[] rowBounds 0 : offset 1 : limit
		 */
		public Page(int[] rowBounds, boolean count) {
			super(0);
			if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
				pageSizeZero = true;
				this.pageSize = 0;
			} else {
				this.pageSize = rowBounds[1];
				this.pageNum = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
			}
			this.startRow = rowBounds[0];
			this.count = count;
			this.endRow = this.startRow + rowBounds[1];
		}

		public List<E> getResult() {
			return this;
		}

		public int getPages() {
			return pages;
		}

		public Page<E> setPages(int pages) {
			this.pages = pages;
			return this;
		}

		public int getEndRow() {
			return endRow;
		}

		public Page<E> setEndRow(int endRow) {
			this.endRow = endRow;
			return this;
		}

		public int getPageNum() {
			return pageNum;
		}

		public Page<E> setPageNum(int pageNum) {
			// 分页合理化，针对不合理的页码自动处理
			this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
			return this;
		}

		public int getPageSize() {
			return pageSize;
		}

		public Page<E> setPageSize(int pageSize) {
			this.pageSize = pageSize;
			return this;
		}

		public int getStartRow() {
			return startRow;
		}

		public Page<E> setStartRow(int startRow) {
			this.startRow = startRow;
			return this;
		}

		public long getTotal() {
			return total;
		}

		public void setTotal(long total) {
			this.total = total;
			if (total == -1) {
				pages = 1;
				return;
			}
			if (pageSize > 0) {
				pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
			} else {
				pages = 0;
			}
			// 分页合理化，针对不合理的页码自动处理
			if ((reasonable != null && reasonable) && pageNum > pages) {
				pageNum = pages;
				calculateStartAndEndRow();
			}
		}

		public Boolean getReasonable() {
			return reasonable;
		}

		public Page<E> setReasonable(Boolean reasonable) {
			if (reasonable == null) {
				return this;
			}
			this.reasonable = reasonable;
			// 分页合理化，针对不合理的页码自动处理
			if (this.reasonable && this.pageNum <= 0) {
				this.pageNum = 1;
				calculateStartAndEndRow();
			}
			return this;
		}

		public Boolean getPageSizeZero() {
			return pageSizeZero;
		}

		public Page<E> setPageSizeZero(Boolean pageSizeZero) {
			if (pageSizeZero != null) {
				this.pageSizeZero = pageSizeZero;
			}
			return this;
		}

		/**
		 * 计算起止行号
		 */
		private void calculateStartAndEndRow() {
			this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
			this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
		}

		public boolean isCount() {
			return this.count;
		}

		public Page<E> setCount(boolean count) {
			this.count = count;
			return this;
		}

		public String getOrderBy() {
			return orderBy;
		}

		public Page<E> setOrderBy(String orderBy) {
			this.orderBy = orderBy;
			return this;
		}

		public boolean isOrderByOnly() {
			return orderByOnly;
		}

		public void setOrderByOnly(boolean orderByOnly) {
			this.orderByOnly = orderByOnly;
		}

		public Boolean getCountSignal() {
			return countSignal;
		}

		public void setCountSignal(Boolean countSignal) {
			this.countSignal = countSignal;
		}

		// 增加链式调用方法

		/**
		 * 设置页码
		 *
		 * @param pageNum
		 * @return
		 */
		public Page<E> pageNum(int pageNum) {
			// 分页合理化，针对不合理的页码自动处理
			this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
			return this;
		}

		/**
		 * 设置页面大小
		 *
		 * @param pageSize
		 * @return
		 */
		public Page<E> pageSize(int pageSize) {
			this.pageSize = pageSize;
			calculateStartAndEndRow();
			return this;
		}

		/**
		 * 是否执行count查询
		 *
		 * @param count
		 * @return
		 */
		public Page<E> count(Boolean count) {
			this.count = count;
			return this;
		}

		/**
		 * 设置合理化
		 *
		 * @param reasonable
		 * @return
		 */
		public Page<E> reasonable(Boolean reasonable) {
			setReasonable(reasonable);
			return this;
		}

		/**
		 * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
		 *
		 * @param pageSizeZero
		 * @return
		 */
		public Page<E> pageSizeZero(Boolean pageSizeZero) {
			setPageSizeZero(pageSizeZero);
			return this;
		}

		/**
		 * 转换为PageInfo
		 *
		 * @return
		 */
		public PageInfo<E> toPageInfo() {
			PageInfo<E> pageInfo = new PageInfo<E>(this);
			return pageInfo;
		}

		public Page<E> doSelectPage(ISelect select) {
			select.doSelect();
			return (Page<E>) this;
		}

		public PageInfo<E> doSelectPageInfo(ISelect select) {
			select.doSelect();
			return (PageInfo<E>) this.toPageInfo();
		}

		public long doCount(ISelect select) {
			this.pageSizeZero = true;
			this.pageSize = 0;
			select.doSelect();
			return this.total;
		}

		@Override
		public String toString() {
			return "Page{" + "count=" + count + ", pageNum=" + pageNum + ", pageSize=" + pageSize + ", startRow=" + startRow
					+ ", endRow=" + endRow + ", total=" + total + ", pages=" + pages + ", countSignal=" + countSignal
					+ ", orderBy='" + orderBy + '\'' + ", orderByOnly=" + orderByOnly + ", reasonable=" + reasonable
					+ ", pageSizeZero=" + pageSizeZero + '}';
		}

	}

	/**
	 * Wrap the page results and add multiple attributes of paging. </br>
	 * </br>
	 * Thank you very much {@link com.github.pagehelper.PageInfo}. We are in
	 * full compliance with your agreements.
	 */
	public static class PageInfo<T> implements Serializable {
		private static final long serialVersionUID = 1L;
		// 当前页
		private int pageNum;
		// 每页的数量
		private int pageSize;
		// 当前页的数量
		private int size;
		// 排序
		private String orderBy;

		// 由于startRow和endRow不常用，这里说个具体的用法
		// 可以在页面中"显示startRow到endRow 共size条数据"

		// 当前页面第一个元素在数据库中的行号
		private int startRow;
		// 当前页面最后一个元素在数据库中的行号
		private int endRow;
		// 总记录数
		private long total;
		// 总页数
		private int pages;
		// 结果集
		private List<T> list;

		// 第一页
		private int firstPage;
		// 前一页
		private int prePage;
		// 下一页
		private int nextPage;
		// 最后一页
		private int lastPage;

		// 是否为第一页
		private boolean isFirstPage = false;
		// 是否为最后一页
		private boolean isLastPage = false;
		// 是否有前一页
		private boolean hasPreviousPage = false;
		// 是否有下一页
		private boolean hasNextPage = false;
		// 导航页码数
		private int navigatePages;
		// 所有导航页号
		private int[] navigatepageNums;

		public PageInfo() {
		}

		/**
		 * 包装Page对象
		 *
		 * @param list
		 */
		public PageInfo(List<T> list) {
			this(list, 8);
		}

		/**
		 * 包装Page对象
		 *
		 * @param list
		 *            page结果
		 * @param navigatePages
		 *            页码数量
		 */
		public PageInfo(List<T> list, int navigatePages) {
			if (list instanceof Page) {
				Page<T> page = (Page<T>) list;
				this.pageNum = page.getPageNum();
				this.pageSize = page.getPageSize();
				this.orderBy = page.getOrderBy();

				this.pages = page.getPages();
				this.list = page;
				this.size = page.size();
				this.total = page.getTotal();
				// 由于结果是>startRow的，所以实际的需要+1
				if (this.size == 0) {
					this.startRow = 0;
					this.endRow = 0;
				} else {
					this.startRow = page.getStartRow() + 1;
					// 计算实际的endRow（最后一页的时候特殊）
					this.endRow = this.startRow - 1 + this.size;
				}
			} else if (list instanceof Collection) {
				this.pageNum = 1;
				this.pageSize = list.size();

				this.pages = 1;
				this.list = list;
				this.size = list.size();
				this.total = list.size();
				this.startRow = 0;
				this.endRow = list.size() > 0 ? list.size() - 1 : 0;
			}
			if (list instanceof Collection) {
				this.navigatePages = navigatePages;
				// 计算导航页
				calcNavigatepageNums();
				// 计算前后页，第一页，最后一页
				calcPage();
				// 判断页面边界
				judgePageBoudary();
			}
		}

		/**
		 * 计算导航页
		 */
		private void calcNavigatepageNums() {
			// 当总页数小于或等于导航页码数时
			if (pages <= navigatePages) {
				navigatepageNums = new int[pages];
				for (int i = 0; i < pages; i++) {
					navigatepageNums[i] = i + 1;
				}
			} else { // 当总页数大于导航页码数时
				navigatepageNums = new int[navigatePages];
				int startNum = pageNum - navigatePages / 2;
				int endNum = pageNum + navigatePages / 2;

				if (startNum < 1) {
					startNum = 1;
					// (最前navigatePages页
					for (int i = 0; i < navigatePages; i++) {
						navigatepageNums[i] = startNum++;
					}
				} else if (endNum > pages) {
					endNum = pages;
					// 最后navigatePages页
					for (int i = navigatePages - 1; i >= 0; i--) {
						navigatepageNums[i] = endNum--;
					}
				} else {
					// 所有中间页
					for (int i = 0; i < navigatePages; i++) {
						navigatepageNums[i] = startNum++;
					}
				}
			}
		}

		/**
		 * 计算前后页，第一页，最后一页
		 */
		private void calcPage() {
			if (navigatepageNums != null && navigatepageNums.length > 0) {
				firstPage = navigatepageNums[0];
				lastPage = navigatepageNums[navigatepageNums.length - 1];
				if (pageNum > 1) {
					prePage = pageNum - 1;
				}
				if (pageNum < pages) {
					nextPage = pageNum + 1;
				}
			}
		}

		/**
		 * 判定页面边界
		 */
		private void judgePageBoudary() {
			isFirstPage = pageNum == 1;
			isLastPage = pageNum == pages;
			hasPreviousPage = pageNum > 1;
			hasNextPage = pageNum < pages;
		}

		public int getPageNum() {
			return pageNum;
		}

		public void setPageNum(int pageNum) {
			this.pageNum = pageNum;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public String getOrderBy() {
			return orderBy;
		}

		public void setOrderBy(String orderBy) {
			this.orderBy = orderBy;
		}

		public int getStartRow() {
			return startRow;
		}

		public void setStartRow(int startRow) {
			this.startRow = startRow;
		}

		public int getEndRow() {
			return endRow;
		}

		public void setEndRow(int endRow) {
			this.endRow = endRow;
		}

		public long getTotal() {
			return total;
		}

		public void setTotal(long total) {
			this.total = total;
		}

		public int getPages() {
			return pages;
		}

		public void setPages(int pages) {
			this.pages = pages;
		}

		public List<T> getList() {
			return list;
		}

		public void setList(List<T> list) {
			this.list = list;
		}

		public int getFirstPage() {
			return firstPage;
		}

		public void setFirstPage(int firstPage) {
			this.firstPage = firstPage;
		}

		public int getPrePage() {
			return prePage;
		}

		public void setPrePage(int prePage) {
			this.prePage = prePage;
		}

		public int getNextPage() {
			return nextPage;
		}

		public void setNextPage(int nextPage) {
			this.nextPage = nextPage;
		}

		public int getLastPage() {
			return lastPage;
		}

		public void setLastPage(int lastPage) {
			this.lastPage = lastPage;
		}

		public boolean isIsFirstPage() {
			return isFirstPage;
		}

		public void setIsFirstPage(boolean isFirstPage) {
			this.isFirstPage = isFirstPage;
		}

		public boolean isIsLastPage() {
			return isLastPage;
		}

		public void setIsLastPage(boolean isLastPage) {
			this.isLastPage = isLastPage;
		}

		public boolean isHasPreviousPage() {
			return hasPreviousPage;
		}

		public void setHasPreviousPage(boolean hasPreviousPage) {
			this.hasPreviousPage = hasPreviousPage;
		}

		public boolean isHasNextPage() {
			return hasNextPage;
		}

		public void setHasNextPage(boolean hasNextPage) {
			this.hasNextPage = hasNextPage;
		}

		public int getNavigatePages() {
			return navigatePages;
		}

		public void setNavigatePages(int navigatePages) {
			this.navigatePages = navigatePages;
		}

		public int[] getNavigatepageNums() {
			return navigatepageNums;
		}

		public void setNavigatepageNums(int[] navigatepageNums) {
			this.navigatepageNums = navigatepageNums;
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("PageInfo{");
			sb.append("pageNum=").append(pageNum);
			sb.append(", pageSize=").append(pageSize);
			sb.append(", size=").append(size);
			sb.append(", startRow=").append(startRow);
			sb.append(", endRow=").append(endRow);
			sb.append(", total=").append(total);
			sb.append(", pages=").append(pages);
			sb.append(", list=").append(list);
			sb.append(", firstPage=").append(firstPage);
			sb.append(", prePage=").append(prePage);
			sb.append(", nextPage=").append(nextPage);
			sb.append(", lastPage=").append(lastPage);
			sb.append(", isFirstPage=").append(isFirstPage);
			sb.append(", isLastPage=").append(isLastPage);
			sb.append(", hasPreviousPage=").append(hasPreviousPage);
			sb.append(", hasNextPage=").append(hasNextPage);
			sb.append(", navigatePages=").append(navigatePages);
			sb.append(", navigatepageNums=");
			if (navigatepageNums == null)
				sb.append("null");
			else {
				sb.append('[');
				for (int i = 0; i < navigatepageNums.length; ++i)
					sb.append(i == 0 ? "" : ", ").append(navigatepageNums[i]);
				sb.append(']');
			}
			sb.append('}');
			return sb.toString();
		}
	}

	/**
	 * Pagination query interface. </br>
	 * </br>
	 * Thank you very much {@link com.github.pagehelper.ISelect}. We are in full
	 * compliance with your agreements.
	 */
	public static interface ISelect {

		/**
		 * 在接口中调用自己的查询方法，不要在该方法内写过多代码，只要一行查询方法最好
		 */
		void doSelect();

	}

	/**
	 * Pagination provider class of {@link com.github.pagehelper.Page}.
	 */
	private static final Class<?> PAGE_CLASS;

	static {
		Class<?> pageClass = null;
		try {
			pageClass = ClassUtils.forName("com.github.pagehelper.Page", PageModel.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			// pageClass = null;
			throw new IllegalStateException(e);
		} catch (LinkageError e) {
			throw new IllegalStateException(e);
		}
		PAGE_CLASS = pageClass;
	}

}